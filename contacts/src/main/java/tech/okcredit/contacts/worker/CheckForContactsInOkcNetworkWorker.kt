package tech.okcredit.contacts.worker

import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseCoroutineWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.RateLimit
import tech.okcredit.contacts.usecase.CheckForContactsInOkcNetwork
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CheckForContactsInOkcNetworkWorker @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) {

    companion object {
        private const val WORKER_NAME = "contacts/contactNetwork"
        const val WORKER_NAME_FCM = "contacts/contactNetworkFcm"
    }

    fun schedule(skipRateLimit: Boolean, workerName: String?): Completable {
        return Single.fromCallable {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return@fromCallable OneTimeWorkRequest.Builder(Worker::class.java)
                .addTag(workerName ?: WORKER_NAME)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 4, TimeUnit.HOURS)
                .build()
                .enableWorkerLogging()
        }.flatMapCompletable { workRequest ->
            if (skipRateLimit.not()) {
                val hours =
                    firebaseRemoteConfig.get().getLong(RateLimit.FRC_KEY_NON_CRITICAL_DATA_WORKER_RATE_LIMIT_HOURS)
                val rateLimit = RateLimit(hours, TimeUnit.HOURS)

                workManager.get().scheduleWithRateLimitRx(
                    workerName ?: WORKER_NAME,
                    Scope.Individual,
                    ExistingWorkPolicy.KEEP,
                    workRequest,
                    rateLimit,
                )
            } else {
                Completable.fromAction {
                    workManager.get().schedule(
                        workerName ?: WORKER_NAME,
                        Scope.Individual,
                        ExistingWorkPolicy.KEEP,
                        workRequest,
                    )
                }
            }
        }.subscribeOn(schedulerProvider.get().io())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val checkForContactsInOkcNetwork: Lazy<CheckForContactsInOkcNetwork>,
    ) : BaseCoroutineWorker(context, params) {

        override suspend fun doActualWork() = checkForContactsInOkcNetwork.get().execute()

        class Factory @Inject constructor(
            private val checkForContactsInOkcNetwork: Lazy<CheckForContactsInOkcNetwork>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(
                    context = context,
                    params = params,
                    checkForContactsInOkcNetwork = checkForContactsInOkcNetwork,
                )
            }
        }
    }
}
