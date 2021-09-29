package `in`.okcredit.merchant.customer_ui.analytics

import `in`.okcredit.analytics.AnalyticsEvents
import `in`.okcredit.analytics.AnalyticsEvents.ADD_TXN_CONFIRM
import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyKey.COMMON_LEDGER
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.SuperProperties
import `in`.okcredit.merchant.customer_ui.addrelationship.analytics.AddRelationshipEventTracker.PropertyKey.DEFAULT_MODE
import dagger.Lazy
import merchant.android.okstream.contract.OkStreamService
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import javax.inject.Inject

class CustomerEventTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
    private val okStreamService: Lazy<OkStreamService>,
) {

    companion object {

        const val ALLOW = "Allow"
        const val DENY = "Deny"

        // Events
        const val PAYMENT_LIMIT_POP_DISPALYED = "Payment limit pop Displayed"
        const val ONLINE_PAYMENT_CLICK = "Online Payment Click"
        const val SELECT_PROFILE = "Select Profile"
        const val SKIP_SELECT_PROFILE = "Skip Select Profile"
        const val UPDATE_PROFILE_FAILED = "Update Profile Failed"
        const val UPDATE_PROFILE = "Update Profile"
        const val ACCOUNT_REPORT_DATE_CLICK = "acct_report_date_click"
        const val ACCOUNT_REPORT_DATE_UPDATE = "acct_report_date_update"
        const val ACCOUNT_REPORT_PREVIEW_LOAD = "acct_report_preview_load"
        const val ACCOUNT_REPORT_DOWNLOAD = "acct_report_download"
        const val SEND_REPORT = "Send Report"
        const val VIEW_STORAGE_PERMISSION = "View Storage Permission"
        const val IN_APP_NOTI_DISPLAYED = "InAppNotification Displayed"
        const val IN_APP_NOTI_CLICKED = "InAppNotification Clicked"
        const val IN_APP_NOTI_CLEARED = "InAppNotification Cleared"
        const val ADD_NOTE_STARTED = "Add Note Started"
        const val PAYMENT_PASSWORD_SKIP = "payment_password_skip"
        const val SELECT_BILL_DATE = "Select Bill Date"
        const val UPDATE_BILL_DATE = "Update Bill Date"
        const val INPUT_CALCULATOR = "Input Calculator"
        const val INPUT_CALCULATOR_ERROR = "Input Calculator Error"
        const val VOICE_NOTE_CLICKED = "voice_note_clicked"
        const val POP_UP_CLOSED = "PopUp Closed"
        const val CALCULATOR_SYMBOLS_USED = "calculator_symbols_clicked"
        const val OPT_OUT_FROM_VOICE_SAMPLES_COLLECTION = "Opt Out Voice Collection"
        const val TRANSACTION_SYNC_ICON_EDUCATION_SHOWN = "TransactionSyncIconEducationShown"
        const val SUPPLIER_TXN_PAGE_API_ERROR = "Supplier Txn Page Api Error"
        const val COLLECT_WITH_GPAY_CLICKED = "collect_with_gpay_clicked"
        const val NO_INTERNET_CONNECTION = "no_internet_connection"
        const val CHOOSE_PAYMENT_OPTION = "Choose payment option"
        const val CONTEXTUAL_TRIGGER_SHOWN = "collections_context_popup_shown"
        const val CONTEXTUAL_TRIGGER_CLICKED = "collections_context_popup_clicked"
        const val ONLINE_PAYMENT_UPDATED_ON_LEDGER = "online_payment_updated_on_ledger"

        // Property Keys
        const val KEY_EASY_PAY = "easy_pay"
        const val RISK_VALUE = "Risk Value"
        const val CASHBACK_MESSAGE_VISIBLE = "Cashback Message visible"
        const val DUE_AMOUNT = "Due Amount"
        const val NO_RESULT = "no_result"
        const val REPORT_DATE = "Report Date"
        const val TYPE = "Type"
        const val MOBILE = "Mobile"
        const val ADDRESS = "Address"
        const val SCREEN = "screen"
        const val RELATION = "Relation"
        const val ACCOUNT_ID = "account_id"
        const val SCREEN_VIEW = "screen_view"
        const val CUSTOMER_ID = "customer_id"
        const val FLOW = "Flow"
        const val DEFAULT = "default"
        const val AMOUNT = "amount"
        const val COLLECTING_SAMPLE = "collecting_sample"
        const val TXN_TYPE = "Txn Type"
        const val ERROR_MSG = "Error Msg"
        const val API_NAME = "Api Name"
        const val IS_CALCULATOR_USED = "if_calculator_used"
        const val TXN_ID = "tx_id"
        const val SOURCE = "Source"
        const val SAMPLE_COUNT = "voice_sample_number"
        const val TRANSCRIBED_TEXT = "transcribed_text"
        const val NOTE_TEXT = "note_text"
        const val TRANSACTION_ID = "transaction_id"
        const val ACTIVITY = "activity"
        const val CONTEXT = "context"
        const val COLLECTION_ID = "collection_id"

        // Property Values
        const val RELATIONSHIP_REMINDER = "Relationship Reminder"
        const val CUSTOMER_CONFLICT = "Customer Conflict"
        const val SUPPLIER_CONFLICT = "Supplier Conflict"
        const val SUPPLIER_DELETED = "Supplier Deleted"
        const val NO_INTERNET = "No Internet"
        const val SOME_ERROR = "Some Error"
        const val INVALID = "Invalid"
        const val MORE_DIGITS = "More Digits"
        const val DATE_RANGE = "date_range"
        const val THIS_MONTH = "this_month"
        const val LAST_MONTH = "last_month"
        const val LAST_SEVEN_DAYS = "last_seven_days"
        const val LAST_ZERO_BALANCE = "last_zero_balance"
        const val LAST_THREE_MONTHS = "last_zero_balance"
        const val LAST_SIX_MONTHS = "last_six_months"
        const val OVERALL = "overall"
        const val RELATION_CUSTOMER = "Customer"
        const val RELATIONSHIP_SCREEN = "Relationship"
        const val CONTEXTUAL_TYPE = "Contextual Permissioning"
        const val DISMISS = "Dismiss"
        const val ADD_TRANSACTION = "Add Transaction"
        const val USER_TRANSACTION_LIMIT = "User Transaction Limit"
        const val AVAILABLE_TRANSACTION_LIMIT = "Available Transaction Limit"
        const val CUSTOMER_SCREEN = "Customer Screen"
        const val RISK_API = "GetPaymentInstruments"
        const val JUSPAY_SUPPLIER_COLLECTION = "Juspay Supplier Collection"
        const val LIMIT_EXCEEDED = "Limit Exceeded"
        const val TXN_COUNT = "Txn count"
        const val TXN_AMOUNT = "Txn amount"
        const val GET_PAYOUT_LINK_DETAILS = "GetPaymentOutLinkDetail"
        const val VOICE_NOTES = "VoiceNotes"
        const val ADD_CUSTOMER = "Add Customer"
        const val SEARCH = "Search"

        const val COLLECTION_INAPP_DISPLAYED = "Collection InApp Displayed"
        const val COLLECTION_INAPP_CLICKED = "Collection InApp Clicked"
        const val COLLECTION_INAPP_CLEARED = "Collection InApp Cleared"

        // Screen
        const val CUSTOMER_REPORTS_SCREEN = "Customer Reports Screen"
        const val CUSTOMER_REPORT_SCREEN = "CustomerReport"
        const val ADD_CUSTOMER_SCREEN = "AddCustomerScreen"

        const val POP_UP_DISPLAYED = "Popup Displayed"
        const val POP_UP_CLICKED = "Popup Clicked"

        const val TRANSACTION_PAGE_LOADED = "transaction_page_load"
        const val TRANSACTION_AMOUNT_ENTERED = "transaction_amount_entered"
        const val TRANSACTION_SCREEN_CLOSED = "transaction_screen_close"
        const val TRANSACTION_SCREEN_FULL_VIEW = "transaction_screen_full_view"

        const val TRANSACTION_FULL_VIEW = "full"
        const val TRANSACTION_MINI = "mini_with_note"

        const val TYPE_PAYMENT_PASSWORD = "Payment Password"
        const val CUSTOMER_SYNC_SUCCESS = "CustomerSyncSuccess"
        const val NOTES = "Notes"
    }

    fun trackPaymentOption(accountId: String, merchantId: String, easyPay: Boolean) {
        val properties = HashMap<String, Any>().apply {
            this[PropertyKey.SCREEN] = PropertyValue.CUSTOMER_SCREEN
            this[PropertyKey.RELATION] = PropertyValue.CUSTOMER
            this[SuperProperties.MERCHANT_ID] = merchantId
            this[KEY_EASY_PAY] = easyPay
            this[PropertyKey.ACCOUNT_ID] = accountId
        }
        analyticsProvider.get().trackEvents(CHOOSE_PAYMENT_OPTION, properties)
    }

    fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        source: String? = null,
        value: Boolean? = null,
        focalArea: Boolean? = null,
        eventProperties: MutableMap<String, Any>? = null,
    ) {
        var properties = eventProperties
        if (properties != null) {
            properties.apply {
                addProperties(screen, type, relation, value, source, focalArea)
            }
        } else {
            properties = mutableMapOf<String, Any>().apply {
                addProperties(screen, type, relation, value, source, focalArea)
            }
        }

        analyticsProvider.get().trackEvents(eventName, properties)
    }

    private fun MutableMap<String, Any>.addProperties(
        screen: String?,
        type: String?,
        relation: String?,
        value: Boolean?,
        source: String?,
        focalArea: Boolean?,
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
        field: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.FIELD to field,
            PropertyKey.ACCOUNT_ID to accountId
        )

        trackEvents(SELECT_PROFILE, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackSkipSelectProfile(
        screen: String,
        relation: String,
        field: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.FIELD to field,
            PropertyKey.ACCOUNT_ID to accountId
        )

        trackEvents(SKIP_SELECT_PROFILE, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackUpdateProfile(
        screen: String,
        relation: String,
        field: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.FIELD to field,
            PropertyKey.ACCOUNT_ID to accountId
        )

        trackEvents(UPDATE_PROFILE, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackUpdateProfileFailed(
        screen: String,
        relation: String,
        field: String,
        accountId: String,
        reason: String?,
    ) {
        val eventProperties = mutableMapOf<String, Any>(

            PropertyKey.REASON to (reason ?: ""),
            PropertyKey.FIELD to field,
            PropertyKey.ACCOUNT_ID to accountId
        )

        trackEvents(UPDATE_PROFILE_FAILED, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackDateRangeClick(
        screen: String,
        relation: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.ACCOUNT_ID to accountId
        )

        trackEvents(ACCOUNT_REPORT_DATE_CLICK, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackDateRangeUpdate(
        screen: String,
        relation: String,
        accountId: String,
        value: String,
        dateRange: MutableList<String>,
    ) {
        val eventProperties = mutableMapOf(
            PropertyKey.ACCOUNT_ID to accountId,
            PropertyKey.VALUE to value,
            DATE_RANGE to dateRange
        )

        trackEvents(ACCOUNT_REPORT_DATE_UPDATE, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackDatePreviewLoad(
        screen: String,
        relation: String,
        accountId: String,
        value: String,
        dateRange: MutableList<String>,
        noResult: Boolean,
    ) {
        val eventProperties = mutableMapOf(
            PropertyKey.ACCOUNT_ID to accountId,
            PropertyKey.VALUE to value,
            DATE_RANGE to dateRange,
            NO_RESULT to noResult,
        )

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
        accountId: String,
        value: String,
        dateRange: MutableList<String>,
        dueAmount: Long,
        collectionAdopted: Boolean,
    ) {
        val eventProperties = mutableMapOf(
            PropertyKey.ACCOUNT_ID to accountId,
            PropertyKey.VALUE to value,
            DATE_RANGE to dateRange,
            PropertyKey.DUE_AMOUNT to dueAmount,
            PropertyKey.COLLECTION_ADOPTED to collectionAdopted
        )

        trackEvents(ACCOUNT_REPORT_DOWNLOAD, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackSendReport(
        screen: String,
        relation: String,
        accountId: String,
        value: String,
        dateRange: MutableList<String>,
        dueAmount: Long,
        collectionAdopted: Boolean,
    ) {
        val eventProperties = mutableMapOf(
            PropertyKey.ACCOUNT_ID to accountId,
            PropertyKey.VALUE to value,
            DATE_RANGE to dateRange,
            PropertyKey.DUE_AMOUNT to dueAmount,
            PropertyKey.COLLECTION_ADOPTED to collectionAdopted
        )

        trackEvents(SEND_REPORT, screen = screen, relation = relation, eventProperties = eventProperties)
    }

    fun trackRuntimePermission(screen: String, type: String, granted: Boolean) {
        if (granted)
            trackEvents(Event.GRANT_PERMISSION, screen = screen, type = type)
        else
            trackEvents(Event.DENY_PERMISSION, screen = screen, type = type)
    }

    fun trackInAppNotificationDisplayed(screen: String? = null, type: String? = null, accountId: String) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.ACCOUNT_ID to accountId
        )
        trackEvents(IN_APP_NOTI_DISPLAYED, screen = screen, type = type, eventProperties = eventProperties)
    }

    fun trackInAppNotificationClicked(
        screen: String? = null,
        type: String? = null,
        focalArea: Boolean? = null,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.ACCOUNT_ID to accountId
        )
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
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.ACCOUNT_ID to accountId
        )
        trackEvents(IN_APP_NOTI_CLEARED, screen = screen, type = type, eventProperties = eventProperties)
    }

    fun trackCustomerTxnAlertPopUpDisplayed(
        customerId: String?,
        relationCustomer: String,
        relationshipScreen: String,
        contextualType: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.RELATION to relationCustomer,
            PropertyKey.SCREEN to relationshipScreen,
            PropertyKey.TYPE to contextualType
        )
        if (customerId.isNotNullOrBlank()) {
            eventProperties[PropertyKey.ACCOUNT_ID] = customerId!!
        }
        trackEvents(POP_UP_DISPLAYED, eventProperties = eventProperties)
    }

    fun trackPopUpClicked(
        customerId: String?,
        relationCustomer: String,
        relationshipScreen: String,
        contextualType: String,
        action: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            PropertyKey.RELATION to relationCustomer,
            PropertyKey.SCREEN to relationshipScreen,
            PropertyKey.TYPE to contextualType,
            PropertyKey.ACTION to action
        )
        if (customerId.isNotNullOrBlank()) {
            eventProperties[PropertyKey.ACCOUNT_ID] = customerId!!
        }
        trackEvents(POP_UP_CLICKED, eventProperties = eventProperties)
    }

    fun trackAddTransactionLoaded(
        type: String,
        screenView: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            TYPE to type,
            RELATION to RELATION_CUSTOMER,
            SCREEN_VIEW to screenView,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        trackEvents(TRANSACTION_PAGE_LOADED, eventProperties = eventProperties)
    }

    fun trackAddTransactionAmountEntered(
        type: String,
        screenView: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            TYPE to type,
            RELATION to RELATION_CUSTOMER,
            SCREEN_VIEW to screenView,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        trackEvents(TRANSACTION_AMOUNT_ENTERED, eventProperties = eventProperties)
    }

    fun trackAddTransactionClosed(
        type: String,
        screenView: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            TYPE to type,
            RELATION to RELATION_CUSTOMER,
            SCREEN_VIEW to screenView,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        trackEvents(TRANSACTION_SCREEN_CLOSED, eventProperties = eventProperties)
    }

    fun trackAddNoteStarted(type: String, screenView: String, accountId: String) {
        val eventProperties = mutableMapOf<String, Any>(
            TYPE to type,
            RELATION to RELATION_CUSTOMER,
            FLOW to ADD_TRANSACTION,
            SCREEN_VIEW to screenView,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        trackEvents(ADD_NOTE_STARTED, eventProperties = eventProperties)
    }

    fun trackAddNoteVoiceClicked(accountId: String, collectingSample: Boolean = false) {
        val eventProperties = mutableMapOf<String, Any>(
            RELATION to RELATION_CUSTOMER,
            FLOW to ADD_TRANSACTION,
            PropertyKey.ACCOUNT_ID to accountId,
            COLLECTING_SAMPLE to collectingSample
        )
        trackEvents(VOICE_NOTE_CLICKED, eventProperties = eventProperties)
    }

    fun trackPaymentPasswordSkip(accountId: String) {
        val eventProperties = mutableMapOf<String, Any>(
            SCREEN to RELATIONSHIP_SCREEN,
            RELATION to RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId
        )
        trackEvents(PAYMENT_PASSWORD_SKIP, eventProperties = eventProperties)
    }

    fun trackUpdateBillDate(
        default: Boolean,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            RELATION to RELATION_CUSTOMER,
            DEFAULT to default,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(UPDATE_BILL_DATE, eventProperties)
    }

    fun trackSelectBillDate(
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            RELATION to RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        trackEvents(SELECT_BILL_DATE, eventProperties = eventProperties)
    }

    fun trackInputCalculator(
        operatorsUsed: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            TYPE to operatorsUsed,
            RELATION to RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(INPUT_CALCULATOR, eventProperties)
    }

    fun trackInputCalculatorError(
        operatorsUsed: String,
        accountId: String,
    ) {
        val eventProperties = mutableMapOf<String, Any>(
            TYPE to operatorsUsed,
            RELATION to RELATION_CUSTOMER,
            PropertyKey.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(INPUT_CALCULATOR_ERROR, eventProperties)
    }

    fun trackTransactionDetails(
        amount: Long,
        billDate: String,
        txnId: String = "not_assigned_yet",
        screen: String = "AddTransactionScreen",
        customerId: String,
    ) {
        val properties = mapOf<String, Any>(
            PropertyKey.AMOUNT to amount,
            PropertyKey.BillDate to billDate,
            PropertyKey.TXN_ID to txnId,
            PropertyKey.SCREEN to screen,
            PropertyKey.CUSTOMER_ID to customerId,
        )
        analyticsProvider.get().trackEvents(Event.TXN_DETAILS_TRACK, properties)
    }

    fun trackInAppNotificationClicked(
        type: String,
        screen: String = "add_txn_screen",
        focalArea: Boolean,
    ) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.TYPE to type,
            PropertyKey.FOCAL_AREA to focalArea,
            PropertyKey.SCREEN to screen
        )
        analyticsProvider.get().trackEvents(AnalyticsEvents.IN_APP_NOTI_CLICKED, eventProperties)
    }

    fun trackInAppNotificationDisplayed(
        type: String,
        screen: String = "add_txn_screen",
    ) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.TYPE to type,
            PropertyKey.SCREEN to screen
        )
        analyticsProvider.get().trackEvents(AnalyticsEvents.IN_APP_NOTI_DISPLAYED, eventProperties)
    }

    fun trackAddTransactionConfirmed(
        type: String,
        customerId: String,
        amount: String,
        relation: String = PropertyValue.CUSTOMER,
        screen: String = "add_txn_screen",
        calculatorUsed: Boolean,
        txnId: String,
        source: String,
        commonLedger: Boolean,
        customerSyncStatus: String,
        notes: String,
    ) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.TYPE to type,
            CUSTOMER_ID to customerId,
            AMOUNT to amount,
            RELATION to relation,
            PropertyKey.SCREEN to screen,
            IS_CALCULATOR_USED to if (calculatorUsed) "yes" else "no",
            TXN_ID to txnId,
            PropertyKey.SOURCE to source,
            COMMON_LEDGER to commonLedger,
            CUSTOMER_SYNC_SUCCESS to customerSyncStatus,
            NOTES to notes,
        )
        analyticsProvider.get().trackEvents(ADD_TXN_CONFIRM, eventProperties)
        // FIXME:: added just for v0 testing
        okStreamService.get().publishAddCustomerTransaction(eventProperties, customerId)
    }

    fun trackPopUpClosed(type: String) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.TYPE to type,
        )
        analyticsProvider.get().trackEvents(POP_UP_CLOSED, eventProperties)
    }

    fun trackOperatorsClicked(customerId: String) {
        val eventProperties = mapOf<String, Any>(
            CUSTOMER_ID to customerId,
        )
        analyticsProvider.get().trackEvents(CALCULATOR_SYMBOLS_USED, eventProperties)
    }

    fun trackOptOutFromVoiceSamplesCollection() {
        analyticsProvider.get().trackEvents(OPT_OUT_FROM_VOICE_SAMPLES_COLLECTION)
    }

    fun trackTransactionSyncedIconEducationShown(screen: String) {
        val properties = mapOf(SCREEN to screen)
        analyticsProvider.get().trackEngineeringMetricEvents(TRANSACTION_SYNC_ICON_EDUCATION_SHOWN, properties)
    }

    fun trackSupplierOnlinePaymentClick(
        accountId: String,
        dueAmount: String,
        screen: String,
        relation: String,
        riskType: String,
        isCashbackMessageVisible: Boolean,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            DUE_AMOUNT to dueAmount,
            SCREEN to screen,
            RELATION to relation,
            RISK_VALUE to riskType,
            CASHBACK_MESSAGE_VISIBLE to isCashbackMessageVisible
        )
        analyticsProvider.get().trackEvents(ONLINE_PAYMENT_CLICK, properties)
    }

    fun trackSupplierPaymentLimitPopDisplayed(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        dueAmount: String,
        type: String,
        userTxnLimit: String,
        availTxnLimit: String,
        txnType: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            DUE_AMOUNT to dueAmount,
            TYPE to type,
            USER_TRANSACTION_LIMIT to userTxnLimit,
            AVAILABLE_TRANSACTION_LIMIT to availTxnLimit,
            TXN_TYPE to txnType
        )

        analyticsProvider.get().trackEvents(PAYMENT_LIMIT_POP_DISPALYED, properties)
    }

    fun trackSupplierTxnPageApiError(
        accountId: String,
        msg: String,
        apiName: String,
        screen: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            ERROR_MSG to msg,
            API_NAME to apiName,
            SCREEN to screen
        )

        analyticsProvider.get().trackEvents(SUPPLIER_TXN_PAGE_API_ERROR, properties)
    }

    fun trackCollectWithGpayClicked(
        accountId: String,
        screen: String,
        amount: Long,
        relation: String = PropertyValue.CUSTOMER,
        type: String, // send or not_sent
        source: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            AMOUNT to amount,
            SCREEN to screen,
            RELATION to relation,
            TYPE to type,
            SOURCE to source,
        )

        analyticsProvider.get().trackEvents(COLLECT_WITH_GPAY_CLICKED, properties)
    }

    fun trackNoInternetError(
        screen: String,
        type: String,
        activity: String,
        flow: String? = null,
        defaultMode: String? = null,
        source: String? = null,
        relation: String? = null
    ) {
        val properties = mutableMapOf(
            SCREEN to screen,
            TYPE to type,
            ACTIVITY to activity
        )

        if (!flow.isNullOrEmpty()) {
            properties[FLOW] = flow
        }
        if (!defaultMode.isNullOrEmpty()) {
            properties[DEFAULT_MODE] = defaultMode
        }
        if (!source.isNullOrEmpty()) {
            properties[SOURCE] = source
        }
        if (!relation.isNullOrEmpty()) {
            properties[RELATION] = relation
        }
        analyticsProvider.get().trackEvents(NO_INTERNET_CONNECTION, properties)
    }

    fun trackContextualTriggerShown(
        accountId: String,
        type: String,
        keyword: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            TYPE to type,
            CONTEXT to keyword
        )

        analyticsProvider.get().trackEvents(CONTEXTUAL_TRIGGER_SHOWN, properties)
    }

    fun trackContextualTriggerClicked(
        accountId: String,
        keyword: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            CONTEXT to keyword
        )

        analyticsProvider.get().trackEvents(CONTEXTUAL_TRIGGER_CLICKED, properties)
    }

    fun trackNewOnlinePaymentShownOnLedger(
        accountId: String,
        collectionId: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            COLLECTION_ID to collectionId
        )

        analyticsProvider.get().trackEvents(ONLINE_PAYMENT_UPDATED_ON_LEDGER, properties)
    }
}
