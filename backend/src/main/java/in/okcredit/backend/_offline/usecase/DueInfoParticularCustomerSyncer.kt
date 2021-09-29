package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.database.internal.DbEntityMapper
import `in`.okcredit.backend._offline.server.BackendRemoteSource
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

class DueInfoParticularCustomerSyncer @Inject constructor(
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val dueInfoRepo: Lazy<DueInfoRepo>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(customerId: String?, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            syncDueInfo(customerId, _businessId)
        }
    }

    private fun syncDueInfo(customerId: String?, businessId: String): Completable {
        return remoteSource.get().getParticularCustomerDueInfo(customerId, businessId)
            .flatMapCompletable { dueInfo ->
                val dueInf = DbEntityMapper.DUE_INFO_API_AND_DB_CONVERTER(businessId).convert(dueInfo)
                dueInfoRepo.get().insertDueInfo(dueInf!!)
            }
    }

    fun schedule(customerId: String, businessId: String): Completable {
        return Completable
            .fromAction {
                val workCategory = "sync_dueInfo_particular_customer"
                val workName = "sync_dueInfo_particular_customer_with_id $customerId"
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest =
                    OneTimeWorkRequest.Builder(Worker::class.java)
                        .addTag(workCategory)
                        .addTag(workName)
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                        .setInputData(
                            workDataOf(
                                Worker.CUSTOMER_ID to customerId,
                                Worker.BUSINESS_ID to businessId
                            )
                        )
                        .build()

                workManager.get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    class Worker(
        context: Context,
        workerParams: WorkerParameters,
        private val dueInfoSyncer: Lazy<DueInfoParticularCustomerSyncer>,
    ) : BaseRxWorker(context, workerParams) {
        override fun doRxWork(): Completable {
            val customerId: String = inputData.getString(CUSTOMER_ID) ?: return Completable.complete()
            val businessId = inputData.getString(BUSINESS_ID)
            return dueInfoSyncer.get().execute(customerId, businessId)
        }

        class Factory @Inject constructor(private val dueInfoSyncer: Lazy<DueInfoParticularCustomerSyncer>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, dueInfoSyncer)
            }
        }

        companion object {
            const val CUSTOMER_ID = "customer_id"
            const val BUSINESS_ID = "business_id"
        }
    }
}
