package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class GetCashbackBannerClosedImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getCashbackBannerClosedImpl: GetCashbackBannerClosedImpl =
        GetCashbackBannerClosedImpl({ collectionRepository }, { getActiveBusinessId })

    @Test
    fun `execute successfully`() {
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getCashbackBannerClosed("customer_id", businessId)).thenReturn(Single.just(true))

        val testObserver = getCashbackBannerClosedImpl.execute("customer_id").test()
        testObserver.assertValue(true)

        verify(collectionRepository).getCashbackBannerClosed("customer_id", businessId)

        testObserver.dispose()
    }
}
