package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test

class SetOnlinePaymentStatusForRefundTxnTest {
    private val collectionRepository: CollectionRepository = mock()
    private val setOnlinePaymentStatusForRefundTxn = SetOnlinePaymentStatusForRefundTxn { collectionRepository }

    @Test
    fun `execute should complete`() {

        whenever(collectionRepository.setOnlinePaymentStatusLocallyForRefundTxn("txn_id", 2))
            .thenReturn(Completable.complete())

        val testObserver =
            setOnlinePaymentStatusForRefundTxn.execute("txn_id", 2).test()

        verify(collectionRepository).setOnlinePaymentStatusLocallyForRefundTxn("txn_id", 2)
        testObserver.assertComplete()
        testObserver.dispose()
    }

    @Test
    fun `execute return error`() {
        val mockError: Exception = mock()

        whenever(collectionRepository.setOnlinePaymentStatusLocallyForRefundTxn("txn_id", 2))
            .thenReturn(Completable.error(mockError))

        val testObserver =
            setOnlinePaymentStatusForRefundTxn.execute("txn_id", 2).test()

        verify(collectionRepository).setOnlinePaymentStatusLocallyForRefundTxn("txn_id", 2)
        testObserver.assertError(mockError)
        testObserver.dispose()
    }
}
