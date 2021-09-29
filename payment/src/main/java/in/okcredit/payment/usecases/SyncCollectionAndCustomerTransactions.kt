package `in`.okcredit.payment.usecases

import `in`.okcredit.backend.contract.SyncTransaction
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SyncCollectionAndCustomerTransactions @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val syncTransactionsImpl: Lazy<SyncTransaction>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
) {

    fun execute(
        syncType: Int = CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS,
    ): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            collectionSyncer.get().scheduleSyncCollections(
                syncType = syncType,
                source = CollectionSyncer.Source.PAYMENT_RESULT,
                businessId = businessId
            )
            syncTransactionsImpl.get().executeForceSync(businessId)
        }
    }
}
