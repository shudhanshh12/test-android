package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.referral.contract.utils.ReferralVersion
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dagger.Lazy
import org.junit.Test

class MenuOptionEventTrackerTest {
    private val analyticsProvider: AnalyticsProvider = mock()
    private val menuSheetOptionEventTracker = MenuOptionEventTracker(Lazy { analyticsProvider })

    @Test
    fun `should call track event with correct name when trackShareReferral is called`() {
        menuSheetOptionEventTracker.trackInviteShareReferral(ReferralVersion.REWARDS_ON_ACTIVATION)
        verify(analyticsProvider).trackEvents(
            "Share Referral",
            mapOf(
                "Version" to "V3",
                "Screen" to "MenuOptionBottomSheet",
                "type" to "general"
            )
        )
    }

    @Test
    fun `should call track event with correct name when trackViewReferral is called`() {
        menuSheetOptionEventTracker.trackViewReferral("Invite Button", ReferralVersion.REWARDS_ON_ACTIVATION)
        verify(analyticsProvider).trackEvents(
            "View Referral",
            mapOf(
                "Version" to "V3",
                "item" to "Invite Button",
                "type" to "general"
            )
        )
    }

    @Test
    fun `should call track event with correct name when trackInviteShareReferral is called`() {
        menuSheetOptionEventTracker.trackMenuOptionViewed(
            mapOf(
                MenuOptionEventTracker.PropertyKey.ITEM to "Invite Customer Viewed",
                MenuOptionEventTracker.PropertyKey.TYPE to ReferralVersion.REWARDS_ON_ACTIVATION

            )
        )
        verify(analyticsProvider).trackObjectViewed(
            "MenuOptionBottomSheet",
            mapOf(
                "item" to "Invite Customer Viewed",
                "type" to ReferralVersion.REWARDS_ON_ACTIVATION
            )
        )
    }

    @Test
    fun `should call track event with correct name when trackMenuScreenInteracted is called`() {
        menuSheetOptionEventTracker.trackMenuScreenInteracted("Invite Button", ReferralVersion.REWARDS_ON_ACTIVATION)

        verify(analyticsProvider).trackObjectInteracted(
            "MenuOptionBottomSheet",
            InteractionType.CLICK,
            mapOf(
                "Version" to "V3", "Type" to "general", "item" to "Invite Button"
            )
        )
    }
}
