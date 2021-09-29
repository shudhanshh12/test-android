package tech.okcredit.home.ui.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.analytics.PropertyValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class HomeEventTrackerTest {

    private val analyticsProvider: AnalyticsProvider = mock()
    private val homeEventTracker = HomeEventTracker { analyticsProvider }

    @Test
    fun `homeScreenViewed() with interaction type should correct event with properties`() {
        homeEventTracker.homeScreenViewed()

        verify(analyticsProvider).trackObjectViewed("Home Screen")
    }

    @Test
    fun `sideMenuViewed() with interaction type should correct event with properties`() {
        homeEventTracker.sideMenuViewed()

        verify(analyticsProvider).trackObjectViewed("Side Menu")
    }

    @Test
    fun `sideMenuInteracted() with interaction type should correct event with properties`() {
        homeEventTracker.sideMenuInteracted("Referral", InteractionType.LONG_PRESS)

        verify(analyticsProvider).trackObjectInteracted(
            "Side Menu", InteractionType.LONG_PRESS,
            mapOf(
                "Item" to "Referral"
            )
        )
    }

    @Test
    fun `sideMenuInteracted() with default interaction type should correct event with properties`() {
        homeEventTracker.sideMenuInteracted("Referral")

        verify(analyticsProvider).trackObjectInteracted(
            "Side Menu", InteractionType.CLICK,
            mapOf(
                "Item" to "Referral"
            )
        )
    }

    @Test
    fun `trackDashboardIconClicked() should correct event with properties`() {
        homeEventTracker.trackDashboardIconClicked("Supplier Tab")

        verify(analyticsProvider).trackEvents(
            "View dashboard",
            mapOf(
                "Source" to "Supplier Tab"
            )
        )
    }

    @Test
    fun `trackDebug() should correct event with properties`() {
        homeEventTracker.trackDebug("Data loading failed", "Out of memory")

        verify(analyticsProvider).trackEngineeringMetricEvents(
            "Debug",
            mapOf(
                "Type" to "Data loading failed",
                "Meta" to "Out of memory"
            )
        )
    }

    @Test
    fun `addTransactionShortcutPageLoad() should correct event with properties`() {
        homeEventTracker.addTransactionShortcutPageLoad(2)

        verify(analyticsProvider).trackEvents(
            "AddTransactionShortcutPageLoad",
            mapOf(
                "suggestion_count" to 2
            )
        )
    }

    @Test
    fun `trackAddTransactionShortcutRelationClicked() should correct event with properties`() {
        homeEventTracker.trackAddTransactionShortcutRelationClicked("Home", "Deeplink", true)

        verify(analyticsProvider).trackEvents(
            "AddTransactionShortcutRelationClicked",
            mapOf(
                "Source" to "Home",
                "flow" to "Deeplink",
                "is_suggested" to true
            )
        )
    }

    @Test
    fun `trackInAppNotificationDisplayed should correct event with properties`() {
        homeEventTracker.trackInAppReviewViewed()

        verify(analyticsProvider).trackEvents("In app review viewed", mapOf())
    }

    @Test
    fun `trackInAppReviewDone should correct event with properties`() {
        homeEventTracker.trackInAppReviewDone()

        verify(analyticsProvider).trackEvents("In app review done", mapOf())
    }

    @Test
    fun `trackInAppNotificationClicked should correct event with properties`() {
        homeEventTracker.trackInAppNotificationClicked(
            PropertyValue.HOME_PAGE, HomeEventTracker.PAY_ONLINE_CUSTOMER, true
        )

        verify(analyticsProvider).trackEvents(
            "InAppNotification Clicked",
            mapOf(
                "Screen" to "Homepage",
                "Type" to "PayOnline Customer",
                "Focal Area" to true
            )
        )
    }

    @Test
    fun `trackFinboxLendingInAppEvent should correct event with properties`() {
        homeEventTracker.trackFinboxLendingInAppEvent("Home")

        verify(analyticsProvider).trackEvents(
            "LEDNING_Finbox In-App notification",
            mapOf(
                "Screen" to "Home"
            )
        )
    }

    @Test
    fun `trackFeedbackNavigationDrawer should correct event with properties`() {
        homeEventTracker.trackFeedbackNavigationDrawer("View Feedbac")

        verify(analyticsProvider).trackEvents(
            "View Feedbac",
            mapOf(
                "Source" to "Drawer",
                "Interaction" to "Started"
            )
        )
    }

    @Test
    fun `trackHomeTabViewed should correct event with properties`() {
        homeEventTracker.trackHomeTabViewed("Customer Tab", false)

        verify(analyticsProvider).trackObjectViewed(
            "Customer Tab",
            mapOf(
                "Payables Experiment" to "Control Group"
            )
        )
    }

    @Test
    fun `trackHomeTabClicked should correct event with properties`() {
        homeEventTracker.trackHomeTabClicked("Customer Tab", false)

        verify(analyticsProvider).trackEvents(
            "Customer Tab Clicked",
            mapOf(
                "Payables Experiment" to "Control Group"
            )
        )
    }

    @Test
    fun `trackPayablesExperimentStarted should correct event with properties`() {
        homeEventTracker.trackPayablesExperimentStarted(false)

        verify(analyticsProvider).trackEvents(
            "Payables Experiment Started",
            mapOf(
                "Payables Experiment" to "Control Group"
            )
        )
    }

    @Test
    fun `trackCallSupportClicked should correct event with properties`() {
        homeEventTracker.trackCallCustomerCareClicked("Side Menu")
        verify(analyticsProvider).trackEvents(
            "Call Customer Care Clicked",
            mapOf(
                "Source" to "Side Menu"
            )
        )
    }
}
