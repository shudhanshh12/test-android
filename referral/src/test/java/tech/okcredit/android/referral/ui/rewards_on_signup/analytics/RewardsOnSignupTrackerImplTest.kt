package tech.okcredit.android.referral.ui.rewards_on_signup.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dagger.Lazy
import org.junit.Test

class RewardsOnSignupTrackerImplTest {
    private val analyticsProvider: AnalyticsProvider = mock()
    private val rewardTracker = RewardsOnSignupTrackerImpl(Lazy { analyticsProvider })

    @Test
    fun `should call track event with correct name when ROA experiment is not enabled, trackReferralFullViewViewed is called`() {
        rewardTracker.trackFullBannerViewed()

        verify(analyticsProvider).trackObjectViewed("Referral Full View")
    }

    @Test
    fun `should call track event with correct name when ROA experiment is not enabled, trackReferralTargetBannerViewed is called`() {
        rewardTracker.trackTargetBannerViewed()

        verify(analyticsProvider).trackObjectViewed("Referral Target Banner")
    }

    @Test
    fun `should call track event with correct name when ROA experiment is not enabled, trackReferralTargetBannerInteracted is called`() {
        rewardTracker.trackTargetBannerInteracted("Banner Clicked")

        verify(analyticsProvider).trackObjectInteracted(
            "Referral Target Banner",
            InteractionType.CLICK,
            mapOf(
                "Item" to "Banner Clicked"
            )
        )
    }
}
