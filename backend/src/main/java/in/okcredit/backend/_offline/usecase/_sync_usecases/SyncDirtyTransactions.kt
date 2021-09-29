package `in`.okcredit.backend._offline.usecase._sync_usecases

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.common.RxJavaUtils
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import android.content.Context
import androidx.work.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.utils.getStringStackTrace
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Syncs all dirty transactions
@Reusable
class SyncDirtyTransactions @Inject
constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val tracker: Lazy<Tracker>,
    private val workManager: Lazy<OkcWorkManager>,
    private val syncDirtyTransaction: Lazy<SyncDirtyTransaction>,
    private val coreSdk: Lazy<CoreSdk>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        const val MAX_COUNT_TRANSACTIONS_PER_EXECUTION_KEY = "max_count_transactions_per_execution"
        const val TAG = "SyncTransactions"
    }

    fun execute(businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                coreSdk.get().isCoreSdkFeatureEnabled(_businessId)
                    .flatMapCompletable {
                        if (it) {
                            coreSyncDirtyTransactions(_businessId)
                        } else {
                            backendSyncDirtyTransactions(_businessId)
                        }
                    }
            }
    }

    private fun coreSyncDirtyTransactions(businessId: String): Completable {
        return coreSdk.get().syncTransactionsCommands(businessId)
    }

    private fun backendSyncDirtyTransactions(businessId: String): Completable {
        Timber.i("$TAG: Started Syncing UnSync Transactions. SyncDirtyTransactions.")
        return transactionRepo.get()
            .listDirtyTransactions(null, businessId)
            .firstOrError()
            .flatMapCompletable { transactions ->
                Timber.i("$TAG: Found ${transactions.size} UnSync Transactions")
                val jobs = ArrayList<Completable>()
                for (transaction in getFilteredTransactions(transactions)) { // Added for avoiding Stack-overflow error.
                    Timber.i("$TAG: Added Job Fob Syncing ${transaction.id}")
                    jobs.add(
                        syncDirtyTransaction.get().execute(transaction.id, businessId)
                            .doOnError {
                                tracker.get().trackTransactionInfo(
                                    Event.TXN_DETAILS_TRACK,
                                    transaction.amountV2.toString(),
                                    transaction.billDate.toString(),
                                    transaction.id,
                                    "SyncDirtyTransactionUsecase",
                                    transaction.customerId,
                                    it.getStringStackTrace()
                                )
                                tracker.get().trackSyncDirtyTransaction("0_Error", transaction.id)
                                tracker.get().trackError("SyncTransaction", "SyncTransaction", it)
                            }
                            .onErrorComplete()
                    )
                }
                RxJavaUtils.runConcurrently(jobs, 4)
            }
    }

    private fun getFilteredTransactions(transactions: List<Transaction>): List<Transaction> {
        val maxCountTransactionsPerExecution =
            firebaseRemoteConfig.get().getLong(MAX_COUNT_TRANSACTIONS_PER_EXECUTION_KEY)
                .toInt()
        return if (transactions.size > maxCountTransactionsPerExecution) transactions.subList(
            0,
            maxCountTransactionsPerExecution
        ) else transactions
    }

    fun schedule(businessId: String): Completable {
        return Completable
            .fromAction {

                val workCategory = "sync_dirty_transactions"
                val workName = "sync_dirty_customers_transactions"

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
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
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
        private val syncDirtyTransactions: Lazy<SyncDirtyTransactions>,
    ) : BaseRxWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)
            return syncDirtyTransactions.get().execute(businessId)
        }

        class Factory @Inject constructor(private val syncDirtyTransactions: Lazy<SyncDirtyTransactions>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncDirtyTransactions)
            }
        }
    }
}
