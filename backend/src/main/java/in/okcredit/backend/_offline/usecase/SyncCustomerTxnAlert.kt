package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class SyncCustomerTxnAlert @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            customerRepo.get().invalidateAllCustomersBuyerTxnAlertFeatureList(_businessId)
        }
    }

    fun schedule(businessId: String): Completable {
        return Completable
            .fromAction {
                val workCategory = "sync_customer_txn_alert"
                val workName = "sync_customer_txn_alert"
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                    .addTag(workCategory)
                    .addTag(workName)
                    .setInputData(
                        workDataOf(
                            Worker.BUSINESS_ID to businessId
                        )
                    )
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                    .build()
                LogUtils.enableWorkerLogging(workRequest)
                workManager.get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncCustomerTxnAlert: Lazy<SyncCustomerTxnAlert>
    ) : BaseRxWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)
            return syncCustomerTxnAlert.get().execute(businessId).onErrorComplete()
        }

        class Factory @Inject constructor(private val syncCustomerTxnAlert: Lazy<SyncCustomerTxnAlert>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncCustomerTxnAlert)
            }
        }
    }
}
