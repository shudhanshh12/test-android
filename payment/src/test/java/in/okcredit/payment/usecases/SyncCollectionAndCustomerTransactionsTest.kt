package `in`.okcredit.payment.usecases

import `in`.okcredit.backend.contract.SyncTransaction
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class SyncCollectionAndCustomerTransactionsTest {
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val syncTransactionsImpl: SyncTransaction = mock()
    private val collectionSyncer: CollectionSyncer = mock()

    private val syncCollectionAndTransactions = SyncCollectionAndCustomerTransactions(
        { getActiveBusinessId },
        { syncTransactionsImpl },
        { collectionSyncer }
    )

    @Test
    fun `execute should return complete`() {
        val businessId = "business-id"
        val source = "source"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(syncTransactionsImpl.execute(source, null, false, businessId)).thenReturn(Completable.complete())

        val testObserver = syncCollectionAndTransactions.execute(source).test()

        verify(getActiveBusinessId).execute()
        verify(collectionSyncer).scheduleSyncCollections(any(), any(), eq(businessId))
        verify(syncTransactionsImpl).executeForceSync(businessId)
        testObserver.assertComplete()
    }
}
