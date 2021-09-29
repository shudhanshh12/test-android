package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend._offline.server.internal.DueInfo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DueInfoSyncer @Inject constructor(
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val dueInfoRepo: Lazy<DueInfoRepo>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                syncDueInfo(_businessId)
            }
    }

    private fun syncDueInfo(businessId: String): Completable {
        return remoteSource.get().getDueInfo(businessId)
            .flatMapCompletable { dueInfos: List<DueInfo> ->
                dueInfoRepo.get().insertAllDueInfo(dueInfos, businessId)
            }
    }

    fun schedule(businessId: String): Completable {
        return Completable
            .fromAction {
                val workName = "sync_dueInfo"
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest =
                    OneTimeWorkRequest.Builder(Worker::class.java)
                        .addTag(workName)
                        .setConstraints(constraints)
                        .setInputData(
                            workDataOf(
                                Worker.BUSINESS_ID to businessId
                            )
                        )
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                        .build()
                workManager.get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    class Worker(context: Context, workerParams: WorkerParameters, private val dueInfoSyncer: Lazy<DueInfoSyncer>) :
        BaseRxWorker(context, workerParams) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)
            return dueInfoSyncer.get().execute(businessId)
        }

        class Factory @Inject constructor(private val dueInfoSyncer: Lazy<DueInfoSyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, dueInfoSyncer)
            }
        }
    }
}
