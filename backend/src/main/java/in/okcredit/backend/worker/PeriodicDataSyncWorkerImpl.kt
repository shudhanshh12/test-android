package `in`.okcredit.backend.worker

import `in`.okcredit.backend.contract.PeriodicDataSyncWorker
import `in`.okcredit.backend.usecase.PeriodicDataSyncer
import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.RateLimit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PeriodicDataSyncWorkerImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) : PeriodicDataSyncWorker {
    companion object {
        private const val WORKER_NAME = "PeriodicDataSyncWorker"
        const val PERIODIC_SYNCER_FLAG_KEY = "periodic_syncer_flag"
        const val FRC_KEY_PERIODIC_DATA_SYNC_WORKER_RATE_LIMIT_HOURS = "periodic_data_sync_worker_rate_limit_hours"
    }

    override fun schedule(businessId: String): Completable {
        return Single.fromCallable {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            return@fromCallable OneTimeWorkRequest.Builder(Worker::class.java)
                .addTag(WORKER_NAME)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        Worker.BUSINESS_ID to businessId
                    )
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        }.flatMapCompletable { workRequest ->
            val isPeriodicSyncEnabled = firebaseRemoteConfig.get().getBoolean(PERIODIC_SYNCER_FLAG_KEY)
            if (isPeriodicSyncEnabled.not()) return@flatMapCompletable Completable.complete()

            val hours = firebaseRemoteConfig.get().getLong(FRC_KEY_PERIODIC_DATA_SYNC_WORKER_RATE_LIMIT_HOURS)

            val rateLimit = RateLimit(hours, TimeUnit.HOURS)

            workManager.get().scheduleWithRateLimitRx(
                WORKER_NAME,
                Scope.Business(businessId),
                ExistingWorkPolicy.KEEP,
                workRequest,
                rateLimit,
            )
        }.subscribeOn(schedulerProvider.get().io())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val periodicDataSyncer: Lazy<PeriodicDataSyncer>,
    ) : BaseRxWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)
            return periodicDataSyncer.get().execute(businessId)
        }

        class Factory @Inject constructor(
            private val periodicDataSyncer: Lazy<PeriodicDataSyncer>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(
                    context = context,
                    params = params,
                    periodicDataSyncer = periodicDataSyncer,
                )
            }
        }
    }
}
