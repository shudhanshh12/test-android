package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class GetOnlinePaymentsTotalTest {

    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getOnlinePaymentsTotal = GetOnlinePaymentsTotal(collectionRepository, { getActiveBusinessId })

    @Test
    fun `execute should return total`() {
        val total = 200.0
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getOnlinePaymentsTotalAmount(businessId)).thenReturn(total)
        val result = getOnlinePaymentsTotal.execute().test()
        assert(result.values().last() == total)
    }
}
