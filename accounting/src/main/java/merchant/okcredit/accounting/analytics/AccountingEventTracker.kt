package merchant.okcredit.accounting.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyKey.FLOW
import `in`.okcredit.analytics.PropertyKey.SCREEN
import `in`.okcredit.analytics.PropertyKey.SORT_BY
import `in`.okcredit.analytics.PropertyKey.SOURCE
import `in`.okcredit.analytics.PropertyKey.STATUS
import dagger.Lazy
import merchant.okcredit.accounting.analytics.AccountingEventTracker.PropertyValue.BOTTOM
import merchant.okcredit.accounting.analytics.AccountingEventTracker.PropertyValue.BUTTON
import merchant.okcredit.accounting.analytics.AccountingEventTracker.PropertyValue.NO
import merchant.okcredit.accounting.analytics.AccountingEventTracker.PropertyValue.SORT_TRANSACTION
import merchant.okcredit.accounting.analytics.AccountingEventTracker.PropertyValue.YES
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import javax.inject.Inject

class AccountingEventTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) {

    companion object {
        // Events
        const val DATE_PICKER_DIALOG_SHOWN = "Date picker dialog shown"
        const val TRANSACTION_FILTER_POPUP_SHOWN = "Transaction Filter Popup Shown"
        const val CUSTOMER_SUPPORT_LEDGER_MSG_SHOWN = "customer_support_ledger_message_shown"
        const val CUSTOMER_SUPPORT_MSG_CLICKED = "customer_support_message_clicked"
        const val LEDGER_POP_UP_ACTION = "cus_support_contact_ledger_popup_action"
        const val EXIT_POP_UP_ACTION = "customer_support_contact_exit_popup_action"
        const val CASHBACK_MSG_LEDGER_SHOWN = "cashback_message_ledger_shown"
        const val SCROLL = "scroll"

        // Property Keys
        const val TYPE = "type"
        const val DATE_RANGE = "date_range"
        const val RELATION = "relation"
        const val ACCOUNT_ID = "account_id"
        const val TXN_ID = "txn_id"
        const val PAYMENT_STATUS = "payment_status"
        const val AMOUNT = "amount"
        const val CUSTOMER_SUPPORT_MESSAGE = "customer_support_message"
        const val CUSTOMER_SUPPORT_NUMBER = "customer_support_number"
        const val ACTION = "action"
        const val CASHBACK_MSG_SHOWN = "cashback_message_shown"
        const val DIRECTION = "direction"
        const val METHOD = "method"

        // Screen
        const val SUPPLIER_STATEMENT_SCREEN = "Supplier statement Screen"
        const val TXN_PAGE_VIEW = "transaction_page_view"

        const val CUSTOMER_SUPPORT_TYPE = "customer_support_message_type"
        const val TXN_AMOUNT = "amount"
        const val TXN_SOURCE = "source"
        const val SOURCE_LEDGER = "ledger"
        const val SOURCE_TXN_PAGE = "transaction_page_view"

        const val STATUS_PENDING = "Pending"
    }

    object PropertyValue {
        const val YES = "yes"
        const val NO = "no"
        const val BALANCE_WIDGET = "balance_widget"
        const val LEDGER_TXN = "ledger_transaction"
        const val SORT_TRANSACTION = "sort_transaction"
        const val BOTTOM = "bottom"
        const val BUTTON = "button"
    }

    fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        source: String? = null,
        value: Boolean? = null,
        focalArea: Boolean? = null,
        eventProperties: HashMap<String, Any>? = null,
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
            this[SOURCE] = source
        }

        focalArea?.let {
            this[PropertyKey.FOCAL_AREA] = focalArea
        }
    }

    fun dateContainerEvent(screen: String, type: String, relation: String) {
        trackEvents(DATE_PICKER_DIALOG_SHOWN, screen = screen, type = type, relation = relation)
    }

    fun onlineTransactionEvent(screen: String, type: String, relation: String) {
        trackEvents(TRANSACTION_FILTER_POPUP_SHOWN, screen = screen, type = type, relation = relation)
    }

    fun allTransactionEvent(screen: String, type: String, relation: String) {
        trackEvents(TRANSACTION_FILTER_POPUP_SHOWN, screen = screen, type = type, relation = relation)
    }

    fun trackViewTransaction(
        screen: String,
        relation: String,
        mobile: String? = null,
        accountId: String? = null,
        flow: String? = null,
        type: String? = null,
        status: String? = null,
        blocked: Boolean? = false,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.SCREEN] = screen
        properties[PropertyKey.RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[PropertyKey.ACCOUNT_ID] = accountId
        if (!flow.isNullOrBlank()) properties[PropertyKey.FLOW] = flow
        if (!type.isNullOrBlank()) properties[PropertyKey.TYPE] = type
        if (!status.isNullOrBlank()) properties[STATUS] = status
        if (blocked != null) properties[PropertyKey.BLOCKED] = blocked
        analyticsProvider.get().trackEvents(Event.VIEW_TRANSACTION, properties)
    }

    fun trackCustomerSupportLedgerMsgShown(
        accountId: String,
        txnId: String,
        amount: String,
        relation: String,
        status: String,
        supportMsg: String,

    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            TXN_ID to txnId,
            AMOUNT to amount,
            RELATION to relation,
            STATUS to status,
            CUSTOMER_SUPPORT_MESSAGE to supportMsg,
        )

        analyticsProvider.get().trackEvents(CUSTOMER_SUPPORT_LEDGER_MSG_SHOWN, properties)
    }

    fun trackCustomerSupportMsgClicked(
        source: String,
        type: String,
        txnId: String,
        amount: String,
        relation: String,
        status: String,
        supportMsg: String,
    ) {
        val properties = mapOf(
            SOURCE to source,
            TYPE to type,
            TXN_ID to txnId,
            AMOUNT to amount,
            RELATION to relation,
            PAYMENT_STATUS to status,
            CUSTOMER_SUPPORT_MESSAGE to supportMsg,
        )

        analyticsProvider.get().trackEvents(CUSTOMER_SUPPORT_MSG_CLICKED, properties)
    }

    fun trackLedgerPopUpAction(
        accountId: String,
        type: String,
        txnId: String,
        amount: String,
        relation: String,
        supportMsg: String,
        action: String,
        supportNumber: String,
        source: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            TYPE to type,
            TXN_ID to txnId,
            AMOUNT to amount,
            RELATION to relation,
            ACTION to action,
            CUSTOMER_SUPPORT_MESSAGE to supportMsg,
            CUSTOMER_SUPPORT_NUMBER to supportNumber,
            TXN_SOURCE to source,
        )

        analyticsProvider.get().trackEvents(LEDGER_POP_UP_ACTION, properties)
    }

    fun trackExitPopUpAction(
        accountId: String,
        source: String,
        type: String,
        relation: String,
        supportNumber: String,
        action: String,
        supportMsg: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            SOURCE to source,
            TYPE to type,
            RELATION to relation,
            ACTION to action,
            CUSTOMER_SUPPORT_MESSAGE to supportMsg,
            CUSTOMER_SUPPORT_NUMBER to supportNumber,
        )

        analyticsProvider.get().trackEvents(EXIT_POP_UP_ACTION, properties)
    }

    fun trackViewTransaction(
        screen: String,
        relation: String,
        mobile: String? = null,
        accountId: String? = null,
        flow: String? = null,
        type: String? = null,
        status: String? = null,
        blocked: Boolean? = false,
        customerSupportType: String = "",
        customerSupportNumber: String = "",
        customerSupportMessage: String = "",
        amount: String = "",
        cashbackMessageShown: Boolean? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.SCREEN] = screen
        properties[PropertyKey.RELATION] = relation
        if (!accountId.isNullOrBlank()) properties[PropertyKey.ACCOUNT_ID] = accountId
        if (!flow.isNullOrBlank()) properties[PropertyKey.FLOW] = flow
        if (!type.isNullOrBlank()) properties[PropertyKey.TYPE] = type
        if (!status.isNullOrBlank()) properties[STATUS] = status
        if (blocked != null) properties[PropertyKey.BLOCKED] = blocked
        if (customerSupportType.isNotNullOrBlank()) {
            properties[CUSTOMER_SUPPORT_TYPE] = customerSupportType
            properties[CUSTOMER_SUPPORT_NUMBER] = customerSupportNumber
            properties[CUSTOMER_SUPPORT_MESSAGE] = customerSupportMessage
            properties[TXN_AMOUNT] = amount
        }
        cashbackMessageShown?.let {
            properties[PropertyKey.CASHBACK_MSG_SHOWN] = if (it) YES else NO
        }
        analyticsProvider.get().trackEvents(Event.VIEW_TRANSACTION, properties)
    }

    fun trackCashbackMsgShown(
        accountId: String,
        source: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            SOURCE to source,
        )

        analyticsProvider.get().trackEvents(CASHBACK_MSG_LEDGER_SHOWN, properties)
    }

    fun trackSortByClicked(relation: String) {
        val properties = mapOf(
            RELATION to relation,
            FLOW to SORT_TRANSACTION,
        )
        analyticsProvider.get().trackEvents(Event.SELECT_FILTER, properties)
    }

    fun trackSortByUpdated(sortBy: String, relation: String) {
        val properties = mapOf(
            RELATION to relation,
            FLOW to SORT_TRANSACTION,
            SORT_BY to sortBy
        )
        analyticsProvider.get().trackEvents(Event.UPDATE_FILTER, properties)
    }

    fun trackScrollToBottomClicked(relation: String, screen: String) {
        val properties = mapOf(
            RELATION to relation,
            DIRECTION to BOTTOM,
            SCREEN to screen,
            METHOD to BUTTON,
        )
        analyticsProvider.get().trackEvents(SCROLL, properties)
    }
}
