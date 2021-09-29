package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction
import javax.inject.Inject

/**
 * Use case which syncs transaction of customer if a collection entry is created but corresponding txn entry is not present.
 * It also syncs collections if txn entry is present and corresponding collection entry is not present.
 * Finally we sync collection if at least one txn is present with [CollectionStatus] not in COMPLETE, FAILED or REFUNDED.
 */
class SyncCustomerTransactionOrCollection @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(customerId: String): Observable<SyncData> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Observable.combineLatest(
                transactionRepo.get().listOnlineTransactions(customerId, businessId)
                    .distinctUntilChanged { old, new -> old.size == new.size },
                collectionRepository.get().getCollectionsOfCustomerOrSupplier(customerId, businessId)
            ) { transactions, collections ->
                checkIfSyncRequired(transactions, collections)
            }
        }
    }

    private fun checkIfSyncRequired(transactions: List<Transaction>, collections: List<Collection>): SyncData {
        // return early if no online transactions and collection present
        if (transactions.isEmpty() && collections.isEmpty()) return SyncData.NONE

        val collectionsCount = collections.size
        val onlineTransactionsCount = transactions.size

        // if we get collection for customer but no transaction
        if (onlineTransactionsCount == 0 && collectionsCount > 0) {
            return SyncData.TRANSACTION
        }

        // if we get transaction for customer but no collection
        if (onlineTransactionsCount > 0 && collectionsCount == 0) {
            return SyncData.COLLECTION
        }

        // if their is a size mismatch then we would need to sync data
        if (collectionsCount != onlineTransactionsCount) {
            if (collectionsCount < onlineTransactionsCount) {
                return SyncData.COLLECTION
            }

            if (onlineTransactionsCount < collectionsCount) {
                return SyncData.TRANSACTION
            }
        }

        val pendingCollections = collections.filter {
            it.status == CollectionStatus.PAID
        }

        if (pendingCollections.isNotEmpty()) {
            return SyncData.BOTH
        }

        return SyncData.NONE
    }

    enum class SyncData {
        COLLECTION,
        TRANSACTION,
        NONE,
        BOTH
    }
}
