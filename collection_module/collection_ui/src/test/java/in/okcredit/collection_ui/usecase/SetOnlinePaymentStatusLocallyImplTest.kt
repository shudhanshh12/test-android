package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class SetOnlinePaymentStatusLocallyImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val setOnlinePaymentStatusLocallyImpl = SetOnlinePaymentStatusLocallyImpl(
        { collectionRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `execute should complete`() {
        val businessId = "business=id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.setOnlinePaymentStatusLocallyForAllOlderTxn(1, 2, businessId))
            .thenReturn(Completable.complete())

        val testObserver =
            setOnlinePaymentStatusLocallyImpl.execute(1, 2).test()

        verify(collectionRepository).setOnlinePaymentStatusLocallyForAllOlderTxn(1, 2, businessId)
        testObserver.assertComplete()
        testObserver.dispose()
    }

    @Test
    fun `execute return error`() {
        val mockError: Exception = mock()

        val businessId = "business=id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.setOnlinePaymentStatusLocallyForAllOlderTxn(1, 2, businessId))
            .thenReturn(Completable.error(mockError))

        val testObserver =
            setOnlinePaymentStatusLocallyImpl.execute(1, 2).test()

        verify(collectionRepository).setOnlinePaymentStatusLocallyForAllOlderTxn(1, 2, businessId)
        testObserver.assertError(mockError)
        testObserver.dispose()
    }
}
