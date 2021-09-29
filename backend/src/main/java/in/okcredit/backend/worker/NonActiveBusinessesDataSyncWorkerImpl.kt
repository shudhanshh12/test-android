package `in`.okcredit.backend.worker

import `in`.okcredit.backend.contract.NonActiveBusinessesDataSyncWorker
import `in`.okcredit.backend.usecase.NonActiveBusinessesDataSyncer
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.awaitFirst
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.RateLimit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NonActiveBusinessesDataSyncWorkerImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val isMultipleAccountEnabled: Lazy<IsMultipleAccountEnabled>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
) : NonActiveBusinessesDataSyncWorker {
    companion object {
        private const val WORKER_NAME = "NonActiveBusinessesDataSyncWorker"
        const val FRC_NON_ACTIVE_BUSINESSES_DATA_SYNCER_ENABLED = "non_active_businesses_data_syncer_enabled"
        const val FRC_KEY_NON_ACTIVE_BUSINESSES_DATA_SYNC_WORKER_RATE_LIMIT_HOURS =
            "non_active_businesses_data_sync_worker_rate_limit_hours"
    }

    override fun schedule(): Completable = rxCompletable {
        val isMultipleAccountsEnabled = isMultipleAccountEnabled.get().execute().awaitFirst()
        if (isMultipleAccountsEnabled.not()) return@rxCompletable // return if multiple accounts feature not enabled

        val businessCount = getBusinessIdList.get().execute().first().size
        if (businessCount <= 1) return@rxCompletable // return if multiple businesses not present

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
            .addTag(WORKER_NAME)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .build()

        val isNonActiveBusinessesDataSyncEnabled =
            firebaseRemoteConfig.get().getBoolean(FRC_NON_ACTIVE_BUSINESSES_DATA_SYNCER_ENABLED)
        if (isNonActiveBusinessesDataSyncEnabled.not()) return@rxCompletable

        val hours =
            firebaseRemoteConfig.get().getLong(FRC_KEY_NON_ACTIVE_BUSINESSES_DATA_SYNC_WORKER_RATE_LIMIT_HOURS)
        val rateLimit = RateLimit(hours, TimeUnit.HOURS)

        workManager.get().scheduleWithRateLimit(
            uniqueWorkName = WORKER_NAME,
            scope = Scope.Individual,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            workRequest = workRequest,
            rateLimit = rateLimit,
        )
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val nonActiveBusinessesDataSyncer: Lazy<NonActiveBusinessesDataSyncer>,
    ) : BaseRxWorker(context, params) {
        override fun doRxWork(): Completable {
            return nonActiveBusinessesDataSyncer.get().execute()
        }

        class Factory @Inject constructor(
            private val nonActiveBusinessesDataSyncer: Lazy<NonActiveBusinessesDataSyncer>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(
                    context = context,
                    params = params,
                    nonActiveBusinessesDataSyncer = nonActiveBusinessesDataSyncer,
                )
            }
        }
    }
}
