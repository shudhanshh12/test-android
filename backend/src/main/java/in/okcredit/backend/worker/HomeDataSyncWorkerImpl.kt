package `in`.okcredit.backend.worker

import `in`.okcredit.backend.contract.HomeDataSyncWorker
import `in`.okcredit.backend.usecase.HomeDataSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.RateLimit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeDataSyncWorkerImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : HomeDataSyncWorker {
    companion object {
        private const val WORKER_NAME = "HomeDataSyncWorker"
        const val FRC_KEY_HOME_DATA_SYNC_WORKER_RATE_LIMIT_HOURS = "home_data_sync_worker_rate_limit_hours"
    }

    override fun schedule(): Completable {
        return Single.fromCallable {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return@fromCallable OneTimeWorkRequest.Builder(Worker::class.java)
                .addTag(WORKER_NAME)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()
                .enableWorkerLogging()
        }.flatMapCompletable { workRequest ->
            val hours = firebaseRemoteConfig.get().getLong(FRC_KEY_HOME_DATA_SYNC_WORKER_RATE_LIMIT_HOURS)

            getActiveBusinessId.get().execute()
                .flatMapCompletable { businessId ->
                    val rateLimit = RateLimit(hours, TimeUnit.HOURS)

                    workManager.get().scheduleWithRateLimitRx(
                        WORKER_NAME,
                        Scope.Business(businessId),
                        ExistingWorkPolicy.KEEP,
                        workRequest,
                        rateLimit,
                    )
                }
        }.subscribeOn(schedulerProvider.get().io())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val homeDataSyncer: Lazy<HomeDataSyncer>,
    ) : BaseRxWorker(context, params) {

        override fun doRxWork() = homeDataSyncer.get().execute()

        class Factory @Inject constructor(
            private val homeDataSyncer: Lazy<HomeDataSyncer>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(
                    context = context,
                    params = params,
                    homeDataSyncer = homeDataSyncer,
                )
            }
        }
    }
}
