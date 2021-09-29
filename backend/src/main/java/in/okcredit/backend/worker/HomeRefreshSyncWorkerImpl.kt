package `in`.okcredit.backend.worker

import `in`.okcredit.backend.contract.HomeRefreshSyncWorker
import `in`.okcredit.backend.usecase.HomeRefreshSyncer
import `in`.okcredit.backend.usecase.HomeRefreshSyncer.Companion.SOURCE
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import javax.inject.Inject

class HomeRefreshSyncWorkerImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
) : HomeRefreshSyncWorker {
    companion object {
        const val WORKER_NAME = "HomeRefreshSyncWorker"
        private const val KEY_SOURCE = "source"
    }

    override fun schedule(source: String?): Completable {
        return Completable.fromAction {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                .addTag(WORKER_NAME)
                .setConstraints(constraints)
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(WORKER_NAME, Scope.Individual, ExistingWorkPolicy.REPLACE, workRequest)
        }.subscribeOn(schedulerProvider.get().io())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val homeRefreshSyncer: Lazy<HomeRefreshSyncer>,
    ) : BaseRxWorker(context, params) {

        override fun doRxWork(): Completable {
            val source = inputData.getString(KEY_SOURCE) ?: SOURCE
            return homeRefreshSyncer.get().execute(source)
        }

        class Factory @Inject constructor(
            private val homeRefreshSyncer: Lazy<HomeRefreshSyncer>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(
                    context = context,
                    params = params,
                    homeRefreshSyncer = homeRefreshSyncer,
                )
            }
        }
    }
}
