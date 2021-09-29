package tech.okcredit.android.referral.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetReferralLinkImplTest {
    private val referralRepository: ReferralRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var getReferralLinkImpl: GetReferralLinkImpl

    @Before
    fun setUp() {
        getReferralLinkImpl = GetReferralLinkImpl({ referralRepository }, { getActiveBusinessId })
    }

    @Test
    fun testExecute() {
        // given
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(referralRepository.getReferralLink(businessId)).thenReturn(Observable.just("url"))

        // when
        val result = getReferralLinkImpl.execute().test()

        // then
        result.assertValue {
            it.equals("url")
        }
    }
}
