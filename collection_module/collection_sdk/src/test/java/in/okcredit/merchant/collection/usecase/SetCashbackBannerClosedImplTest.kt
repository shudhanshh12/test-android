package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class SetCashbackBannerClosedImplTest {

    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val setCashbackBannerClosedImplTest: SetCashbackBannerClosedImpl =
        SetCashbackBannerClosedImpl({ collectionRepository }, { getActiveBusinessId })

    @Test
    fun `execute successfully`() {
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            collectionRepository.setCashbackBannerClosed(
                "customer_id",
                businessId
            )
        ).thenReturn(Completable.complete())

        val testObserver = setCashbackBannerClosedImplTest.execute("customer_id").test()
        testObserver.assertComplete()

        verify(collectionRepository).setCashbackBannerClosed("customer_id", businessId)

        testObserver.dispose()
    }
}
