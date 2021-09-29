package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetCollectionOnlinePaymentTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val geCollectionOnlinePayment = GetCollectionOnlinePayment(collectionRepository, { getActiveBusinessId })

    @Test
    fun `execute should return collectionOnlinePayment`() {
        val mockResponse: CollectionOnlinePayment = mock()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getOnlinePayment("id", businessId)).thenReturn(Observable.just(mockResponse))

        val testObserver = geCollectionOnlinePayment.execute("id").test()
        verify(collectionRepository).getOnlinePayment("id", businessId)
        testObserver.assertValues(Result.Progress(), Result.Success(mockResponse))
        testObserver.dispose()
    }
}
