package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.collection.CollectionTestData.CUSTOMER_ADDITIONAL_INFO
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class GetTargetedReferralListImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val abRepository: AbRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getTargetedReferralListImpl = GetTargetedReferralListImpl(
        { collectionRepository },
        { abRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `execute when feature is enabled successfully `() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(abRepository.isFeatureEnabled("collection_targeted_referral", businessId = businessId)).thenReturn(
            Observable.create {
                it.onNext(true)
            }
        )

        whenever(collectionRepository.getTargetedReferral(businessId)).thenReturn(
            Observable.create {
                it.onNext(listOf(CUSTOMER_ADDITIONAL_INFO))
            }
        )

        val testObserver = getTargetedReferralListImpl.execute().test()

        assert(testObserver.valueCount() == 1 && testObserver.values().size == 1)

        verify(collectionRepository).getTargetedReferral(businessId)
        verify(abRepository).isFeatureEnabled("collection_targeted_referral", businessId = businessId)
    }

    @Test
    fun `execute when feature is disabled successfully `() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(abRepository.isFeatureEnabled("collection_targeted_referral", businessId = businessId)).thenReturn(
            Observable.create {
                it.onNext(false)
            }
        )

        val testObserver = getTargetedReferralListImpl.execute().test()

        assert(testObserver.valueCount() == 0)

        verify(abRepository).isFeatureEnabled("collection_targeted_referral", businessId = businessId)
    }
}
