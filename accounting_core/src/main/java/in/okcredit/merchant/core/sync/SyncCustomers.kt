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

class SyncCustomers @Inject constructor(
    private val syncCustomerCommands: Lazy<SyncCustomerCommands>,
    private val remoteSource: Lazy<CoreRemoteSource>,
    private val localSource: Lazy<CoreLocalSource>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        @NonNls
        const val TAG = "${CoreSdkImpl.TAG}/SyncCustomers"

        @NonNls
        const val WORKER_TAG = "core"

        @NonNls
        const val WORKER_NAME = "core/syncCustomers"

        private const val BUSINESS_ID = "business_id"
    }

    fun execute(isScheduledExecution: Boolean = false, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            rxCompletable {
                syncCustomerCommands.get().execute(includeImmutableCustomers = isScheduledExecution, _businessId)
            }.andThen(
                remoteSource.get().listCustomers(null, _businessId)
            ).flatMapCompletable { customers ->
                Timber.v("$TAG fetched ${customers.size} customers")
                localSource.get().resetCustomerList(customers, _businessId)
                    .doOnComplete { (Timber.v("$TAG saved ${customers.size} customers")) }
            }
        }
    }

    fun schedule(businessId: String): Completable {
        return Completable
            .fromAction {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val dataBuilder = Data.Builder()
                dataBuilder.putString(BUSINESS_ID, businessId)

                val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                    .addTag(WORKER_TAG)
                    .addTag(WORKER_NAME)
                    .setInputData(dataBuilder.build())
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                    .build()

                LogUtils.enableWorkerLogging(workRequest)

                workManager.get()
                    .schedule(WORKER_NAME, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
            }
            .subscribeOn(ThreadUtils.newThread())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncCustomers: Lazy<SyncCustomers>,
    ) : BaseRxWorker(context, params) {

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)
            return syncCustomers.get().execute(
                isScheduledExecution = true,
                businessId = businessId
            )
        }

        class Factory @Inject constructor(private val syncCustomers: Lazy<SyncCustomers>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncCustomers)
            }
        }
    }
}
