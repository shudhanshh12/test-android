package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomerTxnAlertDialogDismissWorker @Inject constructor(
    private val remoteSource: BackendRemoteSource,
    private val customerRepo: Lazy<CustomerRepo>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(customerId: String, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                dismissCustomerTxnAlert(customerId, _businessId)
            }
    }

    private fun dismissCustomerTxnAlert(customerId: String, businessId: String): Completable {
        return remoteSource.updateFeatureValueRequest(customerId, ALLOWED, businessId).andThen(
            Completable.fromAction {
                val map = customerRepo.get().getCustomerTxnAlertMap(businessId)
                map?.let {
                    it[customerId] = false
                    customerRepo.get().updateBuyerMap(it, businessId)
                }
            }
        )
    }

    fun schedule(accountID: String, businessId: String): Completable {
        return Completable
            .fromAction {
                val workCategory = "sync_contacts_with_accounts"
                val workName = "sync_contacts_with_accounts"
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                    .addTag(workCategory)
                    .addTag(workName)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                    .setInputData(
                        workDataOf(
                            CUSTOMER_ID to accountID,
                            Worker.BUSINESS_ID to businessId
                        )
                    )
                    .build()
                LogUtils.enableWorkerLogging(workRequest)
                workManager.get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncContactsWithAccount: Lazy<CustomerTxnAlertDialogDismissWorker>,
    ) : BaseRxWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business-id"
        }

        override fun doRxWork(): Completable {
            val customerId = inputData.getString(CUSTOMER_ID)
            val businessId = inputData.getString(BUSINESS_ID)
            customerId?.let {
                return syncContactsWithAccount.get().execute(customerId, businessId).onErrorComplete()
            }
            return Completable.complete()
        }

        class Factory @Inject constructor(
            private val syncContactsWithAccount: Lazy<CustomerTxnAlertDialogDismissWorker>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncContactsWithAccount)
            }
        }
    }

    companion object {
        const val ALLOWED = 1
        const val CUSTOMER_ID = "customer_id"
    }
}
