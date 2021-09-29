package `in`.okcredit.backend._offline.usecase._sync_usecases

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend.contract.SyncCustomers
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

class SyncCustomersImpl @Inject constructor(
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val workManager: Lazy<OkcWorkManager>,
    private val coreSdk: Lazy<CoreSdk>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SyncCustomers {

    companion object {
        const val TAG = "<<<<SyncCustomers"
    }

    override fun execute(businessId: String?): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            coreSdk.get().isCoreSdkFeatureEnabled(_businessId)
                .flatMapCompletable {
                    if (it) {
                        core_execute()
                    } else {
                        backendExecute(_businessId)
                    }
                }
        }
    }

    fun schedule(businessId: String): Completable {
        return coreSdk.get().isCoreSdkFeatureEnabled(businessId)
            .flatMapCompletable {
                if (it) {
                    coreSchedule(businessId)
                } else {
                    backend_schedule(businessId)
                }
            }
    }

    internal fun backendExecute(businessId: String): Completable {
        return remoteSource.get()
            .listCustomers(null, businessId)
            .flatMapCompletable { customers ->
                Timber.v("$TAG fetched ${customers.size} customers")
                customerRepo.get().resetCustomerList(customers.toList(), businessId)
                    .doOnComplete { (Timber.v("$TAG saved ${customers.size} customers")) }
            }
    }

    fun backend_schedule(businessId: String): Completable {
        return Completable
            .fromAction {

                val workCategory = "sync_customers"
                val workName = "sync_customers"

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequest.Builder(SyncCustomersImpl.Worker::class.java)
                    .addTag(workCategory)
                    .addTag(workName)
                    .setInputData(
                        workDataOf(
                            Worker.BUSINESS_ID to businessId
                        )
                    )
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                    .build()

                LogUtils.enableWorkerLogging(workRequest)

                workManager.get()
                    .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    private fun core_execute() = coreSdk.get().syncCustomers()

    private fun coreSchedule(businessId: String) = coreSdk.get().scheduleSyncCustomers(businessId)

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncCustomersImpl: Lazy<SyncCustomersImpl>
    ) : BaseRxWorker(context, params) {

        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)!!
            return syncCustomersImpl.get().backendExecute(businessId)
        }

        class Factory @Inject constructor(private val syncCustomersImpl: Lazy<SyncCustomersImpl>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncCustomersImpl)
            }
        }
    }
}
