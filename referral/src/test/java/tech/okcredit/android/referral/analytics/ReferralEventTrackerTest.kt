package tech.okcredit.android.referral.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.referral.contract.utils.ReferralVersion
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dagger.Lazy
import org.junit.Test

class ReferralEventTrackerTest {

    private val analyticsProvider: AnalyticsProvider = mock()
    private val referralEventTracker = ReferralEventTracker(Lazy { analyticsProvider })

    @Test
    fun `should call track event with correct name when trackSharedOnWhatsApp is called`() {
        referralEventTracker.trackSharedOnWhatsApp()

        verify(analyticsProvider).trackEvents("ShareScreen: Share On Whatsapp Clicked")
    }

    @Test
    fun `should call track event with correct name when trackApkSharedOnWhatsApp is called`() {
        referralEventTracker.trackApkSharedOnWhatsApp()

        verify(analyticsProvider).trackEvents("ShareScreen: APK Share On Whatsapp Clicked")
    }

    @Test
    fun `should call track event with correct name when trackShareAppViewed is called`() {
        referralEventTracker.trackShareAppViewed()

        verify(analyticsProvider).trackObjectViewed("Share App Screen")
    }

    @Test
    fun `should call track event with correct name when trackReferralRewardsViewed is called`() {
        referralEventTracker.trackReferralRewardsViewed(ReferralVersion.REWARDS_ON_ACTIVATION)

        verify(analyticsProvider).trackEvents(
            "Referral Reward Screen Opened",
            mapOf(
                "referrer_version" to "V3",
                "type" to "general"
            )
        )
    }

    @Test
    fun `should call track event with correct name when trackEarnMoreRewardsInteracted is called`() {
        referralEventTracker.trackEarnMoreRewardsInteracted(ReferralVersion.REWARDS_ON_ACTIVATION)

        verify(analyticsProvider).trackEvents(
            "Referral See more details clicked",
            mapOf(
                "referrer_version" to "V3",
                "type" to "general"
            )
        )
    }

    @Test
    fun `should call track event with correct name when trackNotifyMerchantInteracted is called`() {
        referralEventTracker.trackNotifyMerchantInteracted(ReferralVersion.REWARDS_ON_ACTIVATION)

        verify(analyticsProvider).trackEvents(
            "Notify button clicked",
            mapOf(
                "referrer_version" to "V3",
                "type" to "general"
            )
        )
    }

    @Test
    fun `should call track event with correct name when trackUnclaimedRewardsInteracted is called`() {
        referralEventTracker.trackUnclaimedRewardsInteracted(ReferralVersion.REWARDS_ON_ACTIVATION)

        verify(analyticsProvider).trackEvents(
            "Claim Referral amount button Clicked",
            mapOf(
                "referrer_version" to "V3",
                "type" to "general"
            )
        )
    }

    @Test
    fun `should call track event with correct name when trackReferredUserQualification is called`() {
        referralEventTracker.trackReferredUserQualification(1)

        verify(analyticsProvider).trackEvents("Referred User Qualification", mapOf("qualified" to 1))
    }

    @Test
    fun `should call track event with correct name when trackShareAppInteracted is called`() {
        referralEventTracker.trackShareAppInteracted("button", InteractionType.LONG_PRESS)

        verify(analyticsProvider).trackObjectInteracted(
            "Share App Screen",
            InteractionType.LONG_PRESS,
            mapOf("item" to "button")
        )
    }
}
