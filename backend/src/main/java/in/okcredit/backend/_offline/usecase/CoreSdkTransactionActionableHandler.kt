package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.analytics.CoreTracker.DebugType.TRANSACTION_RECOVERY_ERROR
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import org.apache.commons.jcs.access.exception.InvalidArgumentException
import tech.okcredit.android.base.crashlytics.RecordException
import timber.log.Timber
import javax.inject.Inject

class CoreSdkTransactionActionableHandler @Inject constructor(
    private val coreSdk: Lazy<CoreSdk>,
    private val tracker: Lazy<CoreTracker>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    companion object {
        const val MAX_TRANSACTION_IDS_PER_REQUEST = 100
    }

    fun handle(actionId: String?, startTime: Long?, endTime: Long?, businessId: String? = null): Single<Boolean> {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMap { _businessId ->
            handleAction(actionId, startTime, endTime, _businessId)
        }
    }

    private fun handleAction(actionId: String?, startTime: Long?, endTime: Long?, businessId: String): Single<Boolean> {
        return if (actionId != null && actionId.isNotEmpty() && startTime != null && endTime != null) {
            coreSdk.get().getTransactionsIdsByCreatedTime(startTime, endTime, businessId)
                .flatMap { transactionIds ->
                    val transactionIdsChunked = transactionIds.chunked(MAX_TRANSACTION_IDS_PER_REQUEST)
                    val completableList = transactionIdsChunked.map { transactionIdsChunk ->
                        coreSdk.get().bulkSearchTransactions(actionId, transactionIdsChunk, businessId)
                            .flatMapCompletable { missingTransactionIds ->
                                if (missingTransactionIds.isNotEmpty()) { // Missing transactions present!
                                    coreSdk.get().markTransactionsDirtyAndCreateCommands(missingTransactionIds, businessId)
                                } else { // No missing transactions
                                    Completable.complete()
                                }
                            }
                    }
                    Completable.concat(completableList)
                        .andThen(Single.just(true))
                }
        } else {
            val e = InvalidArgumentException(
                "Incorrect arguments in CoreSdkTransactionActionableHandler.handle(): " +
                    "actionId: $actionId, startTime: $startTime, endTime: $endTime"
            )
            RecordException.recordException(e)
            Timber.e(e)
            tracker.get()
                .trackDebug(TRANSACTION_RECOVERY_ERROR, "actionId: $actionId, startTime: $startTime, endTime: $endTime")
            Single.just(false)
        }
    }
}
