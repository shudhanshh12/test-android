package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CustomerAdditionalInfo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class ShouldShowReferralBannerTest {
    private val collectionRepository: CollectionRepository = mock()
    private val abRepository: AbRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val shouldShowReferralBanner: ShouldShowReferralBanner =
        ShouldShowReferralBanner({ collectionRepository }, { abRepository }, { getActiveBusinessId })

    @Test
    fun `when at least one entry in customerAdditionalInfo table and feature enabled`() {

        val customerInfoList = listOf(CustomerAdditionalInfo("", "", 1, 1, "", "", "", false))
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getTargetedReferral(businessId))
            .thenReturn(Observable.just(customerInfoList))
        whenever(abRepository.isFeatureEnabled("collection_targeted_referral"))
            .thenReturn(Observable.just(true))

        val testObserver =
            shouldShowReferralBanner.execute().test().awaitCount(1)
        testObserver.assertValue(true)

        verify(collectionRepository).getTargetedReferral(businessId)
        verify(abRepository).isFeatureEnabled("collection_targeted_referral")
        testObserver.dispose()
    }

    @Test
    fun `when no  entries in customerAdditionalInfo table and feature enabled`() {

        val customerInfoList = listOf<CustomerAdditionalInfo>()
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getTargetedReferral(businessId))
            .thenReturn(Observable.just(customerInfoList))
        whenever(abRepository.isFeatureEnabled("collection_targeted_referral"))
            .thenReturn(Observable.just(true))

        val testObserver =
            shouldShowReferralBanner.execute().test().awaitCount(1)
        testObserver.assertValue(false)

        verify(collectionRepository).getTargetedReferral(businessId)
        verify(abRepository).isFeatureEnabled("collection_targeted_referral")
        testObserver.dispose()
    }

    @Test
    fun `when feature is not enabled  should return false`() {
        val customerInfoList = listOf<CustomerAdditionalInfo>()

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getTargetedReferral(businessId))
            .thenReturn(Observable.just(customerInfoList))
        whenever(abRepository.isFeatureEnabled("collection_targeted_referral"))
            .thenReturn(Observable.just(false))

        val testObserver =
            shouldShowReferralBanner.execute().test().awaitCount(1)
        testObserver.assertValue(false)

        verify(abRepository).isFeatureEnabled("collection_targeted_referral")
        testObserver.dispose()
    }
}
