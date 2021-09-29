package `in`.okcredit.backend._offline.usecase._sync_usecases

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
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
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncCustomer @Inject
constructor(
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val workManager: Lazy<OkcWorkManager>,
    private val coreSdk: Lazy<CoreSdk>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(customerId: String, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                coreSdk.get().isCoreSdkFeatureEnabled(_businessId)
                    .flatMapCompletable {
                        if (it) {
                            coreExecute(customerId, _businessId)
                        } else {
                            backendExecute(customerId, _businessId)
                        }
                    }
            }
    }

    fun schedule(customerId: String, businessId: String): Completable {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapCompletable {
                if (it) {
                    coreSchedule(customerId, businessId)
                } else {
                    backendSchedule(customerId, businessId)
                }
            }
    }

    private fun backendExecute(customerId: String, businessId: String): Completable {
        return remoteSource.get().getCustomer(customerId, businessId)
            .flatMapCompletable { customer -> customerRepo.get().putCustomer(customer, businessId) }
    }

    private fun backendSchedule(customerId: String, businessId: String): Completable {
        return Completable
            .fromAction {

                val workCategory = "sync_customer"
                val workName = "sync_customer_$customerId"

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                    .addTag(workCategory)
                    .addTag(workName)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                    .setInputData(
                        workDataOf(
                            Worker.CUSTOMER_ID to customerId,
                            Worker.BUSINESS_ID to businessId
                        )
                    )
                    .build()

                LogUtils.enableWorkerLogging(workRequest)

                workManager.get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
                Timber.i("scheduled sync for %s", customerId)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    private fun coreExecute(customerId: String, businessId: String) = coreSdk.get().syncCustomer(customerId, businessId)

    private fun coreSchedule(customerId: String, businessId: String) = coreSdk.get().scheduleSyncCustomer(customerId, businessId)

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncCustomer: Lazy<SyncCustomer>
    ) : BaseRxWorker(context, params) {

        override fun doRxWork(): Completable {
            val customerId = inputData.getString(CUSTOMER_ID)
                ?: return Completable.complete()
            val businessId = inputData.getString(BUSINESS_ID)
            return syncCustomer.get().execute(customerId, businessId)
        }

        companion object {
            const val CUSTOMER_ID = "customer_id"
            const val BUSINESS_ID = "business_id"
        }

        class Factory @Inject constructor(private val syncCustomer: Lazy<SyncCustomer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncCustomer)
            }
        }
    }
}
