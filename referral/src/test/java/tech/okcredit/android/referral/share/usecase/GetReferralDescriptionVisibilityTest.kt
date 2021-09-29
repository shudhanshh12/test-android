package tech.okcredit.android.referral.share.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.referral.share.usecase.GetReferralDescriptionVisibility.Companion.SHOW_REFERRAL_DESCRIPTION

class GetReferralDescriptionVisibilityTest {
    private val mockAb: AbRepository = mock()

    private val getReferralDescriptionVisibility = GetReferralDescriptionVisibility { mockAb }

    @Test
    fun `getReferralDescriptionVisibility should return true when feature is enabled`() {
        whenever(mockAb.isFeatureEnabled(SHOW_REFERRAL_DESCRIPTION)).thenReturn(Observable.just(true))

        val result = getReferralDescriptionVisibility.execute().test()
        result.assertValue(true)
    }

    @Test
    fun `getReferralDescriptionVisibility should return false when feature is disabled`() {
        whenever(mockAb.isFeatureEnabled(SHOW_REFERRAL_DESCRIPTION)).thenReturn(Observable.just(false))

        val result = getReferralDescriptionVisibility.execute().test()
        result.assertValue(false)
    }
}
