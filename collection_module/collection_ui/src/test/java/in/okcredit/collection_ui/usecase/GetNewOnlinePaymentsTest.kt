package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetNewOnlinePaymentsTest {

    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getNewOnlinePayments = GetNewOnlinePayments(collectionRepository, { getActiveBusinessId })

    @Test
    fun `execute should return list of new onlinePayments`() {
        val mockResponse = listOf<CollectionOnlinePayment>(mock(), mock())
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.listOfNewOnlinePayments(businessId)).thenReturn(Observable.just(mockResponse))

        val testObserver = getNewOnlinePayments.execute(Unit).test()

        testObserver.assertValues(Result.Progress(), Result.Success(mockResponse))
        testObserver.dispose()
    }
}
