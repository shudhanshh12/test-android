package `in`.okcredit.merchant.core.sync

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdkImpl
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.store.CoreLocalSource
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncCustomer @Inject constructor(
    private val syncCustomerCommands: Lazy<SyncCustomerCommands>,
    private val remoteSource: Lazy<CoreRemoteSource>,
    private val localSource: Lazy<CoreLocalSource>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        @NonNls
        const val TAG = "${CoreSdkImpl.TAG}/SyncCustomer"

        @NonNls
        const val WORKER_TAG = "core"

        @NonNls
        const val WORKER_NAME = "core/syncCustomer"

        private const val BUSINESS_ID = "business_id"
    }

    fun execute(customerId: String, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            rxCompletable { syncCustomerCommands.get().execute(customerId, _businessId) }
                .andThen(
                    remoteSource.get().getCustomer(customerId, _businessId)
                        .flatMapCompletable { customer -> localSource.get().putCustomer(customer, _businessId) }
                        .doOnComplete { Timber.d("$TAG synced customerId: $customerId") }
                )
        }
    }

    fun schedule(customerId: String, businessId: String): Completable {
        return Completable
            .fromAction {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val dataBuilder = Data.Builder()
                dataBuilder.putString(BUSINESS_ID, businessId)
                dataBuilder.putString(Worker.CUSTOMER_ID, customerId)

                val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                    .addTag(WORKER_TAG)
                    .setInputData(dataBuilder.build())
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                    .build()

                LogUtils.enableWorkerLogging(workRequest)

                workManager.get()
                    .schedule(WORKER_NAME, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
                Timber.i("scheduled sync for %s", customerId)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncCustomer: Lazy<SyncCustomer>,
    ) : BaseRxWorker(context, params) {
        companion object {
            @NonNls
            const val CUSTOMER_ID = "customer_id"
        }

        override fun doRxWork(): Completable {
            val customerId = inputData.getString(CUSTOMER_ID) ?: return Completable.complete()
            val businessId = inputData.getString(BUSINESS_ID)
            return syncCustomer.get().execute(customerId, businessId)
        }

        class Factory @Inject constructor(private val syncCustomer: Lazy<SyncCustomer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncCustomer)
            }
        }
    }
}
