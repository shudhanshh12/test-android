package tech.okcredit.home.ui.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.Event.IN_APP_REVIEW_DONE
import `in`.okcredit.analytics.Event.IN_APP_REVIEW_VIEWED
import `in`.okcredit.analytics.InteractionType
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyKey.SOURCE
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents
import dagger.Lazy
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.home.ui.analytics.HomeEventTracker.Property.FEATURE_NAME
import tech.okcredit.home.ui.analytics.HomeEventTracker.Property.IS_SUGGESTED
import tech.okcredit.home.ui.analytics.HomeEventTracker.Property.PROPERTY_SCREEN
import tech.okcredit.home.ui.analytics.HomeEventTracker.Property.SUGGESTION_COUNT
import javax.inject.Inject

@AppScope
class HomeEventTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) {

    companion object {
        const val RATING = "rating"
        const val RATEUS_SOURCE = "Rate us Drawer"
        const val IN_APP_NOTI_DISPLAYED = "InAppNotification Displayed"
        const val IN_APP_NOTI_CLICKED = "InAppNotification Clicked"
        const val EVENT_DASHBOARD_CLICKED = "View dashboard"
        const val LENDING_FB_IN_APP = "LEDNING_Finbox In-App notification"

        const val NOTIFICATION_TBD = "In app Notification TBD"
        const val NOTIFICATION_DISPLAYED = "In app Notification Displayed"
        const val NOTIFICATION_CLICKED = "In app Notification Clicked"
        const val NOTIFICATION_CLEARED = "In app Notification Cleared "
        const val TYPE_SET_SECURITY_PIN = "Set Security Pin"
        const val TYPE_UPDATE_SECURITY_PIN = "4 digit Pin"
        const val TYPE_BULK_REMINDER = "BULK_REMINDER"
        const val HOME_UN_SYNCED_ACTIVITY = "UnSync Activity"

        const val SET_NEW_PIN = "Set Security Pin"
        const val UPDATE_NEW_PIN = "4 digit Pin"

        // OnboardingPropertyValue
        const val PAY_ONLINE_CUSTOMER = "PayOnline Customer"

        // feedback
        const val FEEDBACK_EVENT = "View Feedback"

        // user Migration
        const val User_Migration = "User Migration"
        // Add Transaction Shortcut

        const val ADD_TRANSACTION_SHORTCUT_PAGE_LOAD = "AddTransactionShortcutPageLoad"
        const val ADD_TRANSACTION_SHORTCUT_RELATION_CLICK = "AddTransactionShortcutRelationClicked"
        const val CLICK_BOTTOM_BAR = "Click bottom bar"

        const val PAYABLES_EXPERIMENT = "Payables Experiment"
        const val PAYABLES_EXPERIMENT_STARTED = "Payables Experiment Started"
        const val SUSPECT_USER_IS_NOT_SUPPLIER = "Suspect user is not supplier"
        const val TEST_GROUP = "Test Group"
        const val CONTROL_GROUP = "Control Group"
        const val INAPP_PAYMENT_REMINDER_MESSAGE_VIEW = "inapp_payment_reminder_message_view"
        const val INAPP_PAYMENT_REMINDER_ACTION = "inapp_payment_reminder_action"

        const val IN_APP_NOTIFICATION_CLICKED = "InAppNotification Clicked"
        const val IN_APP_NOTIFICATION_DISPLAYED = "InAppNotification Displayed"
        const val PRE_NETWORK_ONBOARDED = "pre_networked_onboarded"
        const val CALL_CUSTOMER_CARE_CLICKED = "Call Customer Care Clicked"

        const val VIEW_ADD_RELATIONSHIP_ONBOARDING_CTA = "View Add Relationship Onboarding CTA"
    }

    object Objects {
        const val SCREEN = "Home Screen"
        const val SIDE_MENU = "Side Menu"
        const val STORAGE_PERMISSION = "Storage Permission"
        const val SUPPLIER_TAB = "Supplier Tab"
        const val CUSTOMER_TAB = "Customer Tab"
    }

    object Property {
        const val PROPERTY_SCREEN = "Screen"
        const val ITEM = "Item"
        const val META = "Meta"
        const val SUGGESTION_COUNT = "suggestion_count"
        const val IS_SUGGESTED = "is_suggested"
        const val Flow = "flow"
        const val FEATURE_NAME = "Feature name"

        const val TYPE = "Type"
        const val NOTIFICATION_ID = "NotificationId"
        const val NAME = "Name"
        const val SOURCE = "Source"
        const val PERMISSION = "Permission"
        const val TAB_VIEWED = "tab_viewed"
    }

    private fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        source: String? = null,
        value: Boolean? = null,
        focalArea: Boolean? = null
    ) {
        val properties = HashMap<String, Any>().apply {
            screen?.let {
                this[PropertyKey.SCREEN] = screen
            }

            type?.let {
                this[PropertyKey.TYPE] = type
            }

            relation?.let {
                this[PropertyKey.RELATION] = relation
            }

            value?.let {
                this[PropertyKey.VALUE] = value
            }

            source?.let {
                this[PropertyKey.SOURCE] = source
            }

            focalArea?.let {
                this[PropertyKey.FOCAL_AREA] = focalArea
            }
        }
        analyticsProvider.get().trackEvents(eventName, properties)
    }

    fun trackUpdatePin(eventName: String, flow: String = "", type: String = "", value: String = "") {
        val properties = mapOf(
            PropertyKey.FLOW to flow,
            PropertyKey.TYPE to type,
            PropertyKey.VALUE to value
        )
        analyticsProvider.get().trackEvents(eventName, properties)
    }

    fun trackNavigationDrawer(event: String, properties: Map<String, Any>? = null) {
        val map: MutableMap<String, Any> = properties?.toMutableMap() ?: mutableMapOf()
        map[PropertyKey.SCREEN] = PropertyValue.DRAWER
        analyticsProvider.get().trackEvents(event, map)
    }

    fun trackFeedbackNavigationDrawer(event: String, properties: Map<String, Any>? = null) {
        val map: MutableMap<String, Any> = properties?.toMutableMap() ?: mutableMapOf()
        map[PropertyKey.SOURCE] = PropertyValue.DRAWER
        map[PropertyKey.INTERACTION] = PropertyValue.STARTED
        analyticsProvider.get().trackEvents(event, map)
    }

    fun trackInAppNotificationDisplayed(screen: String? = null, type: String? = null) {
        trackEvents(IN_APP_NOTI_DISPLAYED, screen = screen, type = type)
    }

    fun trackInAppReviewViewed() {
        trackEvents(IN_APP_REVIEW_VIEWED)
    }

    fun trackInAppReviewDone() {
        trackEvents(IN_APP_REVIEW_DONE)
    }

    fun trackInAppNotificationClicked(screen: String? = null, type: String? = null, focalArea: Boolean? = null) {
        trackEvents(IN_APP_NOTI_CLICKED, screen = screen, type = type, focalArea = focalArea)
    }

    fun homeScreenViewed() {
        analyticsProvider.get().trackObjectViewed(Objects.SCREEN)
    }

    fun sideMenuViewed() {
        analyticsProvider.get().trackObjectViewed(Objects.SIDE_MENU)
    }

    fun sideMenuInteracted(item: String, interactionType: InteractionType = InteractionType.CLICK) {
        val properties = mapOf<String, Any>(Property.ITEM to item)
        analyticsProvider.get().trackObjectInteracted(Objects.SIDE_MENU, interactionType, properties)
    }

    fun trackUserMigrationViewed() {
        analyticsProvider.get().trackObjectViewed(User_Migration)
    }

    fun trackStoragePermissionDialog() {
        analyticsProvider.get().trackObjectViewed(Objects.STORAGE_PERMISSION)
    }

    fun trackUserMigrationInteracted(
        item: String,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = mapOf<String, Any>(
            PropertyKey.Item to item,
            PropertyKey.SCREEN to PropertyValue.HOME_PAGE
        )
        analyticsProvider.get().trackObjectInteracted(User_Migration, interactionType, properties)
    }

    fun trackStoragePermissionInteracted(
        item: String,
        interactionType: InteractionType = InteractionType.CLICK
    ) {
        val properties = mapOf<String, Any>(
            PropertyKey.Item to item,
            PropertyKey.SCREEN to PropertyValue.HOME_PAGE

        )
        analyticsProvider.get().trackObjectInteracted(Objects.STORAGE_PERMISSION, interactionType, properties)
    }

    fun trackDashboardIconClicked(source: String) {
        val properties = mapOf(PropertyKey.SOURCE to source)
        analyticsProvider.get().trackEvents(EVENT_DASHBOARD_CLICKED, properties)
    }

    @NonNls
    fun trackDebug(type: String, meta: String) {
        val properties = mapOf(
            PropertyKey.TYPE to type,
            Property.META to meta
        )
        analyticsProvider.get().trackEngineeringMetricEvents(Event.DEBUG, properties)
    }

    fun addTransactionShortcutPageLoad(numberOfSuggestions: Int) {
        val properties = mapOf(SUGGESTION_COUNT to numberOfSuggestions)
        analyticsProvider.get().trackEvents(ADD_TRANSACTION_SHORTCUT_PAGE_LOAD, properties)
    }

    @NonNls
    fun trackAddTransactionShortcutRelationClicked(source: String, flow: String, isSuggested: Boolean) {
        val properties = mapOf(SOURCE to source, IS_SUGGESTED to isSuggested, Property.Flow to flow)
        analyticsProvider.get().trackEvents(ADD_TRANSACTION_SHORTCUT_RELATION_CLICK, properties)
    }

    fun trackFinboxLendingInAppEvent(screen: String) {
        val properties = mapOf(PROPERTY_SCREEN to screen)
        analyticsProvider.get().trackEvents(LENDING_FB_IN_APP, properties)
    }

    fun trackBottomNavigationClickEvents(screen: String) {
        val properties = mapOf(FEATURE_NAME to screen)
        analyticsProvider.get().trackEvents(CLICK_BOTTOM_BAR, properties)
    }

    fun trackHomeTabViewed(objects: String, isExperimentEnabled: Boolean?) {
        val properties = mapOf(
            PAYABLES_EXPERIMENT to
                when (isExperimentEnabled) {
                    null -> SUSPECT_USER_IS_NOT_SUPPLIER
                    true -> TEST_GROUP
                    false -> CONTROL_GROUP
                }
        )
        analyticsProvider.get().trackObjectViewed(objects, properties)
    }

    fun trackHomeTabClicked(
        objects: String,
        isExperimentEnabled: Boolean?,
    ) {
        val properties = mapOf(
            PAYABLES_EXPERIMENT to
                when (isExperimentEnabled) {
                    null -> SUSPECT_USER_IS_NOT_SUPPLIER
                    true -> TEST_GROUP
                    false -> CONTROL_GROUP
                }
        )
        analyticsProvider.get().trackEvents(
            "$objects Clicked",
            properties
        )
    }

    fun trackPayablesExperimentStarted(isExperimentEnabled: Boolean?) {
        val properties = mapOf(
            PAYABLES_EXPERIMENT to
                when (isExperimentEnabled) {
                    null -> SUSPECT_USER_IS_NOT_SUPPLIER
                    true -> TEST_GROUP
                    false -> CONTROL_GROUP
                }
        )
        analyticsProvider.get().trackEvents(PAYABLES_EXPERIMENT_STARTED, properties)
    }

    fun trackInAppNotificationReminderView(
        accountId: String,
        dueAmount: String,
        lastPaymentDate: String?,
        lastPaymentAmount: String?,
        totalReminderLeft: Int,
    ) {
        val properties = HashMap<String, Any>().apply {
            this[SupplierAnalyticsEvents.SupplierProperty.ACCOUNT_ID] = accountId
            this[PropertyKey.SCREEN] = PropertyValue.HOME_PAGE_SCREEN
            this[SupplierAnalyticsEvents.SupplierProperty.DUE_AMOUNT] = dueAmount
            this[SupplierAnalyticsEvents.SupplierProperty.TOTAL_REMINDERS_LEFT] = totalReminderLeft
        }
        lastPaymentAmount?.let {
            properties[SupplierAnalyticsEvents.SupplierProperty.LAST_PAYMENT_AMOUNT] = lastPaymentAmount
        }

        lastPaymentDate?.let {
            properties[SupplierAnalyticsEvents.SupplierProperty.LAST_PAYMENT_DATE] = lastPaymentDate
        }

        analyticsProvider.get().trackEvents(INAPP_PAYMENT_REMINDER_MESSAGE_VIEW, properties)
    }

    fun trackInAppNotificationReminderAction(
        accountId: String,
        dueAmount: String,
        lastPaymentDate: String?,
        lastPaymentAmount: String?,
        totalReminderLeft: Int,
        action: String
    ) {
        val properties = HashMap<String, Any>().apply {
            this[SupplierAnalyticsEvents.SupplierProperty.ACCOUNT_ID] = accountId
            this[PropertyKey.SCREEN] = PropertyValue.HOME_PAGE_SCREEN
            this[SupplierAnalyticsEvents.SupplierProperty.DUE_AMOUNT] = dueAmount
            this[SupplierAnalyticsEvents.SupplierProperty.TOTAL_REMINDERS_LEFT] = totalReminderLeft
            this[SupplierAnalyticsEvents.SupplierProperty.ACTION] = action
        }
        lastPaymentAmount?.let {
            properties[SupplierAnalyticsEvents.SupplierProperty.LAST_PAYMENT_AMOUNT] = lastPaymentAmount
        }

        lastPaymentDate?.let {
            properties[SupplierAnalyticsEvents.SupplierProperty.LAST_PAYMENT_DATE] = lastPaymentDate
        }

        analyticsProvider.get().trackEvents(INAPP_PAYMENT_REMINDER_ACTION, properties)
    }

    fun trackNotificationClicked(
        type: String,
        id: String,
        name: String,
    ) {
        val properties = mapOf(
            Property.TYPE to type,
            Property.NOTIFICATION_ID to id,
            Property.NAME to name,
        )
        analyticsProvider.get().trackEvents(IN_APP_NOTIFICATION_CLICKED, properties)
    }

    fun trackNotificationDisplayed(
        type: String,
        id: String,
        name: String,
    ) {
        val properties = mapOf(
            Property.TYPE to type,
            Property.NOTIFICATION_ID to id,
            Property.NAME to name,
        )
        analyticsProvider.get().trackEvents(IN_APP_NOTIFICATION_DISPLAYED, properties)
    }

    fun trackPreNetworkTabViewed(tabViewed: String) {
        val properties = mapOf(
            Property.TAB_VIEWED to tabViewed
        )
        analyticsProvider.get().trackEvents(PRE_NETWORK_ONBOARDED, properties)
    }

    fun trackCallCustomerCareClicked(source: String) {
        val properties = mapOf(
            Property.SOURCE to source,
        )
        analyticsProvider.get().trackEvents(CALL_CUSTOMER_CARE_CLICKED, properties)
    }

    fun trackViewAddRelationshipOnboardingCTA() {
        analyticsProvider.get().trackEvents(VIEW_ADD_RELATIONSHIP_ONBOARDING_CTA)
    }
}
