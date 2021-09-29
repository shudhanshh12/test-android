package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey.FIELD
import `in`.okcredit.customer.contract.BulkReminderAnalytics
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker.Companion.UPDATE_PROFILE
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderMode
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType
import dagger.Lazy
import javax.inject.Inject

class BulkReminderAnalyticsImpl @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) : BulkReminderAnalytics {
    companion object {

        const val BULK_REMINDER_LIST = "Bulk Reminders List"
        const val ENTRY_POINT_VIEWED = "Entry Point Viewed"
        const val ENTRY_POINT_CLICKED = "Entry Point Clicked"
        const val TAB_CLICKED = "tab_clicked"
        const val SELECT_ALL_CLICKED = "select_all_clicked"
        const val DESELECT_ALL_CLICKED = "deselect_all_clicked"
        const val REMINDER_CLICKED = "reminder_clicked"
        const val REMINDER_LONG_PRESS = "Reminder Long Press"
        const val SELECT_REMINDER_SETTINGS = "Select Reminder Settings"
        const val SEND_REMINDER = "Send Reminder"
        const val BULK_REMINDERS_SEND_STARTED = "bulk_reminders_send_started"
        const val SENDING_REMINDERS_CANCELLED = "sending_reminders_cancelled"
        const val BULK_REMINDER_SEND_COMPLETED = "bulk_reminders_send_completed"
        const val POPUP_DISMISSED = "popup_dismissed"

        const val HOME_BANNER = "home_banner"
        const val BELL_ICON = "Bell Icon"
        const val BANNER = "Banner"
        const val EXPAND = "Expand"
        const val COLLAPSE = "Collapse"
        const val BULK_REMINDER = "Bulk Reminder"
        const val CUSTOMERS = "Customers"
        const val REMINDER_SETTINGS = "Reminder Settings"
        const val REMINDER_SENT = "Reminder Sent"
        const val SENT_TODAY = "Sent Today"
        const val BOTH = "Both"
    }

    object PropertyKey {
        const val TARGET = "Target"
        const val SOURCE = "Source"
        const val TYPE = "Type"
        const val NAME = "Name"
        const val VALUE = "Value"
        const val SCREEN = "Screen"
        const val TAB = "Tab"
        const val BY_DEFAULT = "by_default"
        const val COUNT = "Count"
        const val ACTION = "Action"
        const val ACCOUNT_ID = "Account Id"
        const val RELATION = "Relation"
        const val SET_VALUE = "Set Value"
        const val DUE_SINCE_DAYS = "due_since_days"
        const val DUE_INR = "due_INR"
        const val BALANCE = "Balance"
        const val LAST_REMINDER_SENT_DAYS = "last_reminder_sent_days"
        const val REMINDER_LISTED = "reminders_listed"
        const val CUSTOMERS_REMINDERS_LISTED = "customers_reminders_listed"
        const val SENT_TODAY_REMINDER_LISTED = "sent_today_reminders_listed"
        const val REMINDERS_SELECTED = "reminders_selected"
        const val CUSTOMER_REMINDER_SELECTED = "customers_reminders_selected"
        const val SENT_TODAY_REMINDER_SELECTED = "sent_today_reminders_selected"
        const val CUSTOMERS_SELECT_ALL = "customers_select_all"
        const val REMINDER_SENT = "reminders_sent"
        const val CUSTOMER_REMINDER_SENT = "customers_reminders_sent"
        const val SENT_TODAYS_REMINDER_SENT = "sent_today_reminders_sent"
        const val SENT_TODAY_SELECT_ALL = "sent_today_select_all"
        const val REMINDER_CANCELLED = "reminders_cancelled"
        const val CUSTOMERS_REMINDER_CANCELLED = "customers_reminders_cancelled"
        const val SENT_TODAY_REMINDER_CANCELLED = "sent_today_reminders_cancelled"
        const val POPUP_NAME = "popup_name"
        const val CLICKED_ON = "clicked_on"
    }

    override fun trackEntryPointViewed() {
        val propertiesMap = mutableMapOf(
            PropertyKey.TARGET to HOME_BANNER,
            PropertyKey.SOURCE to BANNER,
            PropertyKey.TYPE to BULK_REMINDER_LIST,
            PropertyKey.NAME to BULK_REMINDER_LIST
        )
        analyticsProvider.get().trackEvents(ENTRY_POINT_VIEWED, propertiesMap)
    }

    override fun trackEntryPointClicked(belliconClicked: Boolean, numberOfReminders: Int) {
        val propertiesMap = mutableMapOf(
            PropertyKey.TARGET to if (belliconClicked) BELL_ICON else HOME_BANNER,
            PropertyKey.SOURCE to BANNER,
            PropertyKey.TYPE to BULK_REMINDER_LIST,
            PropertyKey.VALUE to numberOfReminders
        )
        analyticsProvider.get().trackEvents(ENTRY_POINT_CLICKED, propertiesMap)
    }

    fun trackTabClicked(reminderTabType: ReminderType, count: Int, isCollapse: Boolean) {
        val propertiesMap = mutableMapOf(
            PropertyKey.SCREEN to BULK_REMINDER,
            PropertyKey.NAME to when (reminderTabType) {
                ReminderType.PENDING_REMINDER -> CUSTOMERS
                ReminderType.TODAYS_REMINDER -> SENT_TODAY
            },
            PropertyKey.ACTION to if (isCollapse) COLLAPSE else EXPAND,
            PropertyKey.COUNT to count
        )
        analyticsProvider.get().trackEvents(TAB_CLICKED, propertiesMap)
    }

    fun trackReminderSelectAllClicked(reminderTabType: ReminderType?, byDefault: Boolean) {
        val propertiesMap = mutableMapOf(
            PropertyKey.SCREEN to BULK_REMINDER,
            PropertyKey.TAB to when (reminderTabType) {
                ReminderType.PENDING_REMINDER -> CUSTOMERS
                ReminderType.TODAYS_REMINDER -> SENT_TODAY
                else -> "Unknown"
            },
            PropertyKey.BY_DEFAULT to byDefault
        )
        analyticsProvider.get().trackEvents(SELECT_ALL_CLICKED, propertiesMap)
    }

    fun trackReminderDeselectAllClicked(reminderTabType: ReminderType?) {
        val propertiesMap = mutableMapOf(
            PropertyKey.SCREEN to BULK_REMINDER,
            PropertyKey.TAB to when (reminderTabType) {
                ReminderType.PENDING_REMINDER -> CUSTOMERS
                ReminderType.TODAYS_REMINDER -> SENT_TODAY
                else -> "Unknown"
            },
        )
        analyticsProvider.get().trackEvents(DESELECT_ALL_CLICKED, propertiesMap)
    }

    fun trackReminderLongPress(accountId: String) {
        val propertiesMap = mutableMapOf(
            PropertyKey.ACCOUNT_ID to accountId,
            PropertyKey.TYPE to REMINDER_SETTINGS,
            PropertyKey.SCREEN to BULK_REMINDER,
            PropertyKey.RELATION to CUSTOMERS
        )
        analyticsProvider.get().trackEvents(REMINDER_LONG_PRESS, propertiesMap)
    }

    fun trackSelectReminderSettings(accountId: String, reminderMode: ReminderMode) {
        val propertiesMap = mutableMapOf(
            PropertyKey.ACCOUNT_ID to accountId,
            PropertyKey.SET_VALUE to reminderMode.value,
            PropertyKey.RELATION to CUSTOMERS,
            PropertyKey.SCREEN to BULK_REMINDER
        )
        analyticsProvider.get().trackEvents(SELECT_REMINDER_SETTINGS, propertiesMap)
    }

    fun trackUpdateProfile(accountId: String, reminderMode: ReminderMode?) {
        val propertiesMap = mutableMapOf(
            PropertyKey.ACCOUNT_ID to accountId,
            PropertyKey.SET_VALUE to (reminderMode?.value ?: "Unknown"),
            FIELD to REMINDER_SETTINGS,
            PropertyKey.RELATION to CUSTOMERS,
            PropertyKey.SCREEN to BULK_REMINDER
        )
        analyticsProvider.get().trackEvents(UPDATE_PROFILE, propertiesMap)
    }

    fun trackReminderClicked(
        accountId: String,
        isSelected: Boolean,
        lastReminderSentTime: String,
        dueINR: String,
        dueSinceDays: String,
        reminderTabType: ReminderType?,
    ) {
        val propertiesMap = mutableMapOf(
            PropertyKey.ACCOUNT_ID to accountId,
            PropertyKey.SCREEN to BULK_REMINDER,
            PropertyKey.DUE_SINCE_DAYS to dueSinceDays,
            PropertyKey.DUE_INR to dueINR,
            PropertyKey.ACTION to if (isSelected) "Select" else "Deselect",
            PropertyKey.LAST_REMINDER_SENT_DAYS to lastReminderSentTime,
            PropertyKey.TAB to when (reminderTabType) {
                ReminderType.PENDING_REMINDER -> CUSTOMERS
                ReminderType.TODAYS_REMINDER -> SENT_TODAY
                else -> "Unknown"
            },
        )
        analyticsProvider.get().trackEvents(REMINDER_CLICKED, propertiesMap)
    }

    fun trackBulkReminderSendStarted(
        totalReminder: Int,
        customerReminderListed: Int,
        sendTodayReminderListed: Int,
        reminderSelected: Int,
        customerReminderSelected: Int,
        sendTodayReminderSelected: Int,
        customerSelectAll: Boolean,
        sendTodaySelectAll: Boolean,
    ) {
        val propertiesMap = mutableMapOf(
            PropertyKey.REMINDER_LISTED to totalReminder,
            PropertyKey.CUSTOMERS_REMINDERS_LISTED to customerReminderListed,
            PropertyKey.SENT_TODAY_REMINDER_LISTED to sendTodayReminderListed,
            PropertyKey.REMINDERS_SELECTED to reminderSelected,
            PropertyKey.CUSTOMER_REMINDER_SELECTED to customerReminderSelected,
            PropertyKey.SENT_TODAY_REMINDER_SELECTED to sendTodayReminderSelected,
            PropertyKey.CUSTOMERS_SELECT_ALL to customerSelectAll,
            PropertyKey.SENT_TODAY_SELECT_ALL to sendTodaySelectAll,
            PropertyKey.TAB to when {
                customerReminderSelected > 0 && sendTodayReminderSelected > 0 -> BOTH
                customerReminderSelected > 0 -> CUSTOMERS
                else -> SENT_TODAY
            }
        )
        analyticsProvider.get().trackEvents(BULK_REMINDERS_SEND_STARTED, propertiesMap)
    }

    fun trackBulkReminderSendCompleted(
        totalReminder: Int,
        customerReminderListed: Int,
        sendTodayReminderListed: Int,
        reminderSelected: Int,
        customerReminderSelected: Int,
        sendTodayReminderSelected: Int,
        customerSelectAll: Boolean,
        sendTodaySelectAll: Boolean,
    ) {
        val propertiesMap = mutableMapOf(
            PropertyKey.REMINDER_LISTED to totalReminder,
            PropertyKey.CUSTOMERS_REMINDERS_LISTED to customerReminderListed,
            PropertyKey.SENT_TODAY_REMINDER_LISTED to sendTodayReminderListed,
            PropertyKey.REMINDERS_SELECTED to reminderSelected,
            PropertyKey.CUSTOMER_REMINDER_SELECTED to customerReminderSelected,
            PropertyKey.SENT_TODAY_REMINDER_SELECTED to sendTodayReminderSelected,
            PropertyKey.CUSTOMERS_SELECT_ALL to customerSelectAll,
            PropertyKey.SENT_TODAY_SELECT_ALL to sendTodaySelectAll,
            PropertyKey.TAB to when {
                customerReminderSelected > 0 && sendTodayReminderSelected > 0 -> BOTH
                customerReminderSelected > 0 -> CUSTOMERS
                else -> SENT_TODAY
            }
        )
        analyticsProvider.get().trackEvents(BULK_REMINDER_SEND_COMPLETED, propertiesMap)
    }

    fun trackSendingReminderCancelled(
        totalReminder: Int,
        customerReminderListed: Int,
        sendTodayReminderListed: Int,
        reminderSelected: Int,
        customerReminderSelected: Int,
        sendTodayReminderSelected: Int,
        customerSelectAll: Boolean,
        sendTodaySelectAll: Boolean,
        reminderSent: Int,
        customerReminderSent: Int,
        sentTodayReminderSent: Int,
        reminderCancelled: Int,
        customerReminderCancelled: Int,
        sendTodayReminderCancelled: Int,
    ) {
        val propertiesMap = mutableMapOf(
            PropertyKey.REMINDER_LISTED to totalReminder,
            PropertyKey.CUSTOMERS_REMINDERS_LISTED to customerReminderListed,
            PropertyKey.SENT_TODAY_REMINDER_LISTED to sendTodayReminderListed,
            PropertyKey.REMINDERS_SELECTED to reminderSelected,
            PropertyKey.CUSTOMER_REMINDER_SELECTED to customerReminderSelected,
            PropertyKey.SENT_TODAY_REMINDER_SELECTED to sendTodayReminderSelected,
            PropertyKey.REMINDER_SENT to reminderSent,
            PropertyKey.CUSTOMER_REMINDER_SENT to customerReminderSent,
            PropertyKey.SENT_TODAYS_REMINDER_SENT to sentTodayReminderSent,
            PropertyKey.CUSTOMERS_SELECT_ALL to customerSelectAll,
            PropertyKey.SENT_TODAY_SELECT_ALL to sendTodaySelectAll,
            PropertyKey.TAB to when {
                customerReminderSelected > 0 && sendTodayReminderSelected > 0 -> BOTH
                customerReminderSelected > 0 -> CUSTOMERS
                else -> SENT_TODAY
            },
            PropertyKey.REMINDER_CANCELLED to reminderCancelled,
            PropertyKey.CUSTOMERS_REMINDER_CANCELLED to customerReminderCancelled,
            PropertyKey.SENT_TODAY_REMINDER_CANCELLED to sendTodayReminderCancelled
        )
        analyticsProvider.get().trackEvents(SENDING_REMINDERS_CANCELLED, propertiesMap)
    }

    fun trackPopupDismissed() {
        val propertyMap = mutableMapOf(
            PropertyKey.SCREEN to BULK_REMINDER,
            PropertyKey.POPUP_NAME to REMINDER_SENT,
            PropertyKey.CLICKED_ON to "OK"
        )
        analyticsProvider.get().trackEvents(POPUP_DISMISSED, propertyMap)
    }
}
