package `in`.okcredit.merchant.suppliercredit.tracker

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import javax.inject.Inject

class SupplierEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {

        const val SELECT_PROFILE = "Select Profile"
        const val UPDATE_PROFILE_FAILED = "Update Profile Failed"
        const val UPDATE_PROFILE = "Update Profile"
        const val ACCOUNT_REPORT_DATE_CLICK = "acct_report_date_click"
        const val ACCOUNT_REPORT_DATE_UPDATE = "acct_report_date_update"
        const val ACCOUNT_REPORT_PREVIEW_LOAD = "acct_report_preview_load"
        const val ACCOUNT_REPORT_DOWNLOAD = "acct_report_download"
        const val SEND_REPORT = "Send Report"
        const val ACCOUNT_REPORT_DATE_SELECT = "acct_report_date_select"
        const val ACCOUNT_REPORT_DATE_CANCEL = "acct_report_date_cancel"
        const val ACCOUNT_REPORT_CLICK = "acct_report_click"
        const val VIEW_STORAGE_PERMISSION = "View Storage Permission"
        const val IN_APP_NOTI_DISPLAYED = "InAppNotification Displayed"
        const val IN_APP_NOTI_CLICKED = "InAppNotification Clicked"
        const val IN_APP_NOTI_CLEARED = "InAppNotification Cleared"
        const val IN_APP_REMINDER_RECEIVED = "inapp_reminder_received"

        // Property Keys
        const val NO_RESULT = "no_result"
        const val REPORT_DATE = "Report Date"
        const val COUNT_TOTAL_REMINDERS = "cnt_total_reminders"
        const val START_TIME = "start_time"

        // Property Values
        const val DATE_RANGE = "date_range"
        const val THIS_MONTH = "this_month"
        const val LAST_MONTH = "last_month"
        const val LAST_SEVEN_DAYS = "last_seven_days"
        const val LAST_ZERO_BALANCE = "last_zero_balance"
        const val LAST_THREE_MONTHS = "last_zero_balance"
        const val LAST_SIX_MONTHS = "last_six_months"
        const val OVERALL = "overall"

        // Screen
        const val SUPPLIER_REPORTS_SCREEN = "Supplier Reports Screen"
        const val SUPPLIER_REPORT_SCREEN = "SupplierReport"

        const val POP_UP_DISPLAYED = "Popup Displayed"
        const val POP_UP_CLICKED = "Popup Clicked"
    }

    fun trackInAppReminderReceived(reminderCount: Int, startTime: String) {
        val eventProperties = HashMap<String, Any>().apply {
            this[COUNT_TOTAL_REMINDERS] = reminderCount
            this[START_TIME] = startTime
        }
        analyticsProvider.get().trackEvents(IN_APP_REMINDER_RECEIVED, eventProperties)
    }

    fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        source: String? = null,
        value: Boolean? = null,
        focalArea: Boolean? = null,
        eventProperties: HashMap<String, Any>? = null
    ) {
        var properties = eventProperties
        if (properties != null) {
            properties.apply {
                addProperties(screen, type, relation, value, source, focalArea)
            }
        } else {
            properties = HashMap<String, Any>().apply {
                addProperties(screen, type, relation, value, source, focalArea)
            }
        }

        analyticsProvider.get().trackEvents(eventName, properties)
    }

    private fun HashMap<String, Any>.addProperties(
        screen: String?,
        type: String?,
        relation: String?,
        value: Boolean?,
        source: String?,
        focalArea: Boolean?
    ) {
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

    fun trackSelectProfile(
        screen: String,
        relation: String,
        mobile: String?,
        field: String,
        accountId: String
    ) {
        val eventProperties = HashMap<String, Any>().apply {

            this[PropertyKey.FIELD] = field

            this[PropertyKey.ACCOUNT_ID] = accountId
        }

        trackEvents(SELECT_PROFILE, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackDateRangeClick(screen: String, relation: String, mobile: String?, accountId: String) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
        }

        trackEvents(ACCOUNT_REPORT_DATE_CLICK, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackDateRangeUpdate(
        screen: String,
        relation: String,
        mobile: String?,
        accountId: String,
        value: String,
        dateRange: MutableList<String>
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
            this[PropertyKey.VALUE] = value
            this[DATE_RANGE] = dateRange
        }

        trackEvents(ACCOUNT_REPORT_DATE_UPDATE, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackDatePreviewLoad(
        screen: String,
        relation: String,
        mobile: String?,
        accountId: String,
        value: String,
        dateRange: MutableList<String>,
        noResult: Boolean
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
            this[PropertyKey.VALUE] = value
            this[DATE_RANGE] = dateRange
            this[NO_RESULT] = noResult
        }

        trackEvents(
            ACCOUNT_REPORT_PREVIEW_LOAD,
            screen = screen,
            relation = relation,
            eventProperties = eventProperties
        )
    }

    fun trackDateDownLoad(
        screen: String,
        relation: String,
        mobile: String?,
        accountId: String,
        value: String,
        dateRange: MutableList<String>,
        dueAmount: Long,
        collectionAdopted: Boolean
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
            this[PropertyKey.VALUE] = value
            this[DATE_RANGE] = dateRange
            this[PropertyKey.DUE_AMOUNT] = dueAmount
            this[PropertyKey.COLLECTION_ADOPTED] = collectionAdopted
        }

        trackEvents(ACCOUNT_REPORT_DOWNLOAD, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackSendReport(
        screen: String,
        relation: String,
        mobile: String?,
        accountId: String,
        value: String,
        dateRange: MutableList<String>,
        dueAmount: Long,
        collectionAdopted: Boolean
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
            this[PropertyKey.VALUE] = value
            this[DATE_RANGE] = dateRange
            this[PropertyKey.DUE_AMOUNT] = dueAmount
            this[PropertyKey.COLLECTION_ADOPTED] = collectionAdopted
        }

        trackEvents(SEND_REPORT, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackDateRangeSelect(
        screen: String,
        relation: String,
        mobile: String?,
        accountId: String,
        field: String,
        value: String
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
            this[PropertyKey.FIELD] = field
            this[PropertyKey.VALUE] = value
        }

        trackEvents(ACCOUNT_REPORT_DATE_SELECT, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackDateRangeCancel(
        screen: String,
        relation: String,
        mobile: String?,
        accountId: String
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
        }

        trackEvents(ACCOUNT_REPORT_DATE_CANCEL, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackReportClick(
        screen: String,
        relation: String,
        mobile: String?,
        accountId: String,
        dueAmount: Long,
        collectionAdopted: Boolean
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
            this[PropertyKey.DUE_AMOUNT] = dueAmount
            this[PropertyKey.COLLECTION_ADOPTED] = collectionAdopted
        }

        trackEvents(ACCOUNT_REPORT_CLICK, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackRuntimePermission(screen: String, type: String, granted: Boolean) {
        if (granted)
            trackEvents(Event.GRANT_PERMISSION, screen = screen, type = type)
        else
            trackEvents(Event.DENY_PERMISSION, screen = screen, type = type)
    }

    fun trackInAppNotificationDisplayed(screen: String? = null, type: String? = null, accountId: String) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
        }
        trackEvents(IN_APP_NOTI_DISPLAYED, screen = screen, type = type, eventProperties = eventProperties)
    }

    fun trackInAppNotificationClicked(
        screen: String? = null,
        type: String? = null,
        focalArea: Boolean? = null,
        accountId: String
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
        }
        trackEvents(
            IN_APP_NOTI_CLICKED,
            screen = screen,
            type = type,
            focalArea = focalArea,
            eventProperties = eventProperties
        )
    }

    fun trackInAppNotificationCleared(
        screen: String? = null,
        type: String? = null,
        accountId: String
    ) {
        val eventProperties = HashMap<String, Any>().apply {
            this[PropertyKey.ACCOUNT_ID] = accountId
        }
        trackEvents(IN_APP_NOTI_CLEARED, screen = screen, type = type, eventProperties = eventProperties)
    }

    fun trackCustomerTxnAlertPopUpDisplayed(
        customerId: String?,
        relationCustomer: String,
        relationshipScreen: String,
        contextualType: String
    ) {
        val eventProperties = HashMap<String, Any>().apply {

            this[PropertyKey.RELATION] = relationCustomer
            this[PropertyKey.SCREEN] = relationshipScreen
            this[PropertyKey.TYPE] = contextualType
        }
        if (customerId.isNullOrEmpty().not()) {
            eventProperties[PropertyKey.ACCOUNT_ID] = customerId!!
        }
        trackEvents(POP_UP_DISPLAYED, eventProperties = eventProperties)
    }

    fun trackPopUpClicked(
        customerId: String?,
        relationCustomer: String,
        relationshipScreen: String,
        contextualType: String,
        action: String
    ) {
        val eventProperties = HashMap<String, Any>().apply {

            this[PropertyKey.RELATION] = relationCustomer
            this[PropertyKey.SCREEN] = relationshipScreen
            this[PropertyKey.TYPE] = contextualType
            this[PropertyKey.ACTION] = action
        }
        if (customerId.isNullOrEmpty().not()) {
            eventProperties[PropertyKey.ACCOUNT_ID] = customerId!!
        }
        trackEvents(POP_UP_CLICKED, eventProperties = eventProperties)
    }
}
