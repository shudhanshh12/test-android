package `in`.okcredit.payment.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event.PAYMENT_REWARD_STATUS_PAGE_VIEW
import `in`.okcredit.analytics.Event.PAYMENT_STATUS_REWARD_ICON_CLICKED
import `in`.okcredit.analytics.PropertyKey.COLLECTION_ADOPTED
import `in`.okcredit.analytics.PropertyKey.REWARD_ID
import `in`.okcredit.analytics.PropertyKey.REWARD_STATUS
import `in`.okcredit.analytics.PropertyKey.REWARD_VALUE
import `in`.okcredit.analytics.PropertyKey.SOURCE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.ACCOUNT_ID
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.ACTION
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.ADDRESS
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.AMOUNT
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.AMOUNT_2
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.API_NAME
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.AVAILABLE_TRANSACTION_LIMIT
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.CASHBACK_SEEN_STATUS
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.CUSTOMER_SUPPORT_MESSAGE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.CUSTOMER_SUPPORT_NUMBER
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.CUSTOMER_SUPPORT_TYPE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.DUE_AMOUNT
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.ERROR
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.ERROR_MSG
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.FLOW
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.LIMIT_EXHAUSTED
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.PAYMENT_ID
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.PRE_FILLED_AMOUNT
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.PROVIDER
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.RELATION
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.RISK_VALUE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.SCREEN
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.STATUS
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.TIME_TAKEN
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.TXN_ID
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.TYPE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.TYPE_2
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.USER_TRANSACTION_LIMIT
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.VALUE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.KEY_EASY_PAY
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.LIMIT_LEFT
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_SUMMARY_SCREEN
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.TOTAL_LIMIT
import dagger.Lazy
import javax.inject.Inject

class PaymentAnalyticsEvents @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        const val AMOUNT_ENTERED = "Amount Entered"
        const val PAYMENT_LIMIT_WARNING_DISPALYED = "Payment limit Warning Displayed"
        const val ENTER_PAYMENT_DETAILS = "Enter Payment Details"
        const val CLICK_REQUEST_PAYMENT_DETAILS = "Click Request payment Details"
        const val CHANGE_PAYMENT_DETAILS = "Change Payment Details"
        const val CHOOSE_PAYMENT_OPTION = "Choose payment option"
        const val CONFIRM_PAYMENT_DETAILS = "Confirm Payment Details"
        const val ENTER_INVALID_PAYMENT_DETAILS = "Entered Invalid Payment Details"
        const val PAYMENT_DETAILS_VALIDATED = "Payment Details Validated"
        const val PAYMENT_STATUS_WAITING_PAGE = "payment Status Waiting page"
        const val PAYMENT_STATUS_PAGE_VIEW = "Payment Status Page View"
        const val PAYMENT_STATUS_CLICK = "Payment Status click"
        const val LOADED_PAYMENT_ERROR_PAGE = "Loaded Payment Error page"
        const val CLICK_RETRY_PAYMENT = "Click Retry Payment"
        const val PAYMENT_CLICK_PROCEED_PAYMENT = "Click Proceed Payment"
        const val PAYMENT_SUMMARY_PAGE_VIEWED = "Enter Amount Page view"
        const val PAYMENT_FLOW_API_ERROR = "Payment Flow Api Error"
        const val CUSTOMER_SUPPORT_MSG_CLICKED = "customer_support_message_clicked"
        const val CUSTOMER_SUPPORT_OPT_MSG_VIEW = "cus_support_payment_options_msg_view"
    }

    object PaymentProperty {
        const val ACCOUNT_ID = "account_id"
        const val DUE_AMOUNT = "Due Amount"
        const val SCREEN = "Screen"
        const val RELATION = "Relation"
        const val FLOW = "Flow"
        const val USER_TRANSACTION_LIMIT = "User Transaction Limit"
        const val AVAILABLE_TRANSACTION_LIMIT = "Available Transaction Limit"
        const val LIMIT_EXHAUSTED = "Limit exhausted"
        const val AMOUNT = "Amount"
        const val PRE_FILLED_AMOUNT = "Pre Filled Amount"
        const val RISK_VALUE = "Risk Value"
        const val TYPE = "Type"
        const val TXN_TYPE = "Txn Type"
        const val VALUE = "Value"
        const val ERROR = "Error"
        const val TIME_TAKEN = "Time Taken"
        const val PROVIDER = "Provider"
        const val STATUS = "Status"
        const val PAYMENT_ID = "Payment Id"
        const val ADDRESS = "Address"
        const val ACTION = "Action"
        const val ERROR_MSG = "Error Msg"
        const val API_NAME = "Api Name"
        const val CASHBACK_SEEN_STATUS = "Cashback Seen Status"
        const val TYPE_2 = "type"
        const val TXN_ID = "txn_id"
        const val AMOUNT_2 = "amount"
        const val RELATION_2 = "relation"
        const val STATUS_2 = "status"
        const val CUSTOMER_SUPPORT_TYPE = "customer_support_message_type"
        const val CUSTOMER_SUPPORT_NUMBER = "customer_support_number"
        const val CUSTOMER_SUPPORT_MESSAGE = "customer_support_message"
    }

    object PaymentPropertyValue {

        const val SUPPLIER = "Supplier"
        const val JUSPAY_SUPPLIER_COLLECTION = "Juspay Supplier Collection"
        const val PAYMENT_SUMMARY_SCREEN = "Payment Summary"
        const val INTERNAL_SUPPLIER_COLLECTION = "Internal Supplier Collection"

        const val IFSC = "IFSC"
        const val BANK_ACCOUNT_NUMBER = "Bank Account Number"
        const val UPI_ID = "UPI_ID"
        const val NOT_AWARE = "Not Aware"
        const val UPI = "UPI"
        const val BANK = "BANK"
        const val PAYMENT_ADDRESS_DETAILS = "Payment Address Details"
        const val INVALID_ACCOUNT_NUMBER = "invalid_account_number"
        const val INVALID_IFSC = "invalid_ifsc"

        const val INVALID_PAYMENT_ADDRESS = "invalid_payment_address"
        const val PAYMENT_STATUS = "Payment Status"
        const val SUCCESSFUL = "Successful"
        const val FAILED = "Failed"
        const val PENDING = "Pending"
        const val SHARE = "Share"
        const val DONE = "Done"
        const val RETRY = "Retry"
        const val CANCELLED = "Cancelled"
        const val CUSTOMER = "Customer"
        const val SET_PAYOUT_DESTINATION = "SetPaymentOutDestination"
        const val GET_PAYMENT_ATTRIBUTE = "payment_link/{link_id}/attributes"
        const val GET_JUSPAY_ATTRIBUTE_PROCESS = "juspay/attributes(PROCESS)"
        const val GET_JUSPAY_ATTRIBUTE_INITIATE = "juspay/attributes(INITIATE)"
        const val KEY_EASY_PAY = "easy_pay"
        const val SEEN = "SEEN"
        const val UNSEEN = "UNSEEN"
        const val TOTAL_LIMIT = "total_limit"
        const val LIMIT_LEFT = "limit_left"
        const val ENTER_AMOUNT_PAGE = "enter_amount_page"
        const val CHOOSE_PAYMENT_OPTION = "choose_payment_option"
        const val PAYMENT_STATUS_PAGE_VIEW = "payment_status_page_view"
        const val BLIND_PAY_ENTER_AMOUNT_PAGE = "blindpay_enter_amount_page"
        const val BLIND_PAY_CHOOSE_OPTION_PAGE = "blindpay_choose_payment_option"
        const val I_DONT_KNOW = "i_dont_know"
        const val PAYMENT_OPTIONS = "payment_options"
    }

    fun trackPaymentAmountEntered(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        dueAmount: String,
        userTxnLimit: String,
        availTxnLimit: String,
        limitExhausted: Boolean,
        amount: String,
        preFilledAmount: String,
        riskValue: String,
        easyPay: Boolean = false,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            DUE_AMOUNT to dueAmount,
            USER_TRANSACTION_LIMIT to userTxnLimit,
            AVAILABLE_TRANSACTION_LIMIT to availTxnLimit,
            LIMIT_EXHAUSTED to limitExhausted,
            AMOUNT to amount,
            PRE_FILLED_AMOUNT to preFilledAmount,
            RISK_VALUE to riskValue,
            KEY_EASY_PAY to easyPay
        )

        analyticsProvider.get().trackEvents(AMOUNT_ENTERED, properties)
    }

    fun trackPaymentLimitWarningDisplayed(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        dueAmount: String,
        userTxnLimit: String,
        availTxnLimit: String,
        amount: String,
        preFilledAmount: String,
        easyPay: Boolean = false,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            DUE_AMOUNT to dueAmount,
            USER_TRANSACTION_LIMIT to userTxnLimit,
            AVAILABLE_TRANSACTION_LIMIT to availTxnLimit,
            AMOUNT_ENTERED to amount,
            PRE_FILLED_AMOUNT to preFilledAmount,
            KEY_EASY_PAY to easyPay
        )

        analyticsProvider.get().trackEvents(PAYMENT_LIMIT_WARNING_DISPALYED, properties)
    }

    fun trackChangePaymentDetails(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        dueAmount: String,
        type: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            DUE_AMOUNT to dueAmount,
            TYPE to type,
        )

        analyticsProvider.get().trackEvents(CHANGE_PAYMENT_DETAILS, properties)
    }

    fun trackPaymentStatusWaitingPage(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        easyPay: Boolean = false,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            KEY_EASY_PAY to easyPay
        )

        analyticsProvider.get().trackEvents(PAYMENT_STATUS_WAITING_PAGE, properties)
    }

    fun trackPaymentStatusPageView(
        accountId: String,
        relation: String,
        type: String,
        screen: String,
        flow: String,
        amount: String,
        paymentId: String,
        address: String,
        provider: String,
        status: String,
        timeTaken: String,
        collectionStatus: Boolean,
        easyPay: Boolean = false,
        customerSupportType: String = "",
        customerSupportNumber: String = "",
        customerSupportMessage: String = "",
    ) {
        val properties = mutableMapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            TYPE to type,
            PROVIDER to provider,
            AMOUNT to amount,
            PAYMENT_ID to paymentId,
            ADDRESS to address,
            STATUS to status,
            TIME_TAKEN to timeTaken,
            COLLECTION_ADOPTED to collectionStatus,
            KEY_EASY_PAY to easyPay,
            CUSTOMER_SUPPORT_TYPE to customerSupportType,
            CUSTOMER_SUPPORT_NUMBER to customerSupportNumber,
            CUSTOMER_SUPPORT_MESSAGE to customerSupportMessage,
        ).apply {
            if (customerSupportType.isNotBlank()) {
                put(CUSTOMER_SUPPORT_TYPE, customerSupportType)
                put(CUSTOMER_SUPPORT_MESSAGE, customerSupportMessage)
                put(CUSTOMER_SUPPORT_NUMBER, customerSupportNumber)
            }
        }

        analyticsProvider.get().trackEvents(PAYMENT_STATUS_PAGE_VIEW, properties)
    }

    fun trackPaymentStatusClick(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        action: String,
        easyPay: Boolean = false,
        status: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            ACTION to action,
            KEY_EASY_PAY to easyPay,
            STATUS to status,
        )

        analyticsProvider.get().trackEvents(PAYMENT_STATUS_CLICK, properties)
    }

    fun trackLoadedPaymentErrorPage(
        accountId: String,
        relation: String,
        type: String,
        screen: String,
        flow: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            TYPE to type
        )

        analyticsProvider.get().trackEvents(LOADED_PAYMENT_ERROR_PAGE, properties)
    }

    fun trackClickedRetryPayment(
        accountId: String,
        relation: String,
        type: String,
        screen: String,
        flow: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            TYPE to type
        )

        analyticsProvider.get().trackEvents(CLICK_RETRY_PAYMENT, properties)
    }

    fun trackClickProceedPayment(
        accountId: String,
        relation: String,
        screen: String,
        dueAmount: String,
        amount: String,
        type: String,
        riskType: String,
        easyPay: Boolean = false,
        totalLimit: Long? = null,
        limitLeft: Long? = null,
    ) {
        val properties = mutableMapOf<String, Any>(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            DUE_AMOUNT to dueAmount,
            TYPE to type,
            AMOUNT to amount,
            RISK_VALUE to riskType,
            KEY_EASY_PAY to easyPay
        )

        if (totalLimit != null) properties[TOTAL_LIMIT] = totalLimit
        if (limitLeft != null) properties[LIMIT_LEFT] = limitLeft

        analyticsProvider.get().trackEvents(PAYMENT_CLICK_PROCEED_PAYMENT, properties)
    }

    fun trackPaymentSummaryPageViewed(
        accountId: String,
        dueAmount: String,
        userTxnLimit: String,
        availTxnLimit: String,
        limitExhausted: Boolean,
        preFilledAmount: String,
        riskValue: String,
        amount: String,
        relation: String,
        easyPay: Boolean = false,
        customerSupportType: String = "",
        customerSupportNumber: String = "",
        customerSupportMessage: String = "",
    ) {
        val properties = mutableMapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to PAYMENT_SUMMARY_SCREEN,
            FLOW to JUSPAY_SUPPLIER_COLLECTION,
            DUE_AMOUNT to dueAmount,
            USER_TRANSACTION_LIMIT to userTxnLimit,
            AVAILABLE_TRANSACTION_LIMIT to availTxnLimit,
            LIMIT_EXHAUSTED to limitExhausted,
            PRE_FILLED_AMOUNT to preFilledAmount,
            RISK_VALUE to riskValue,
            AMOUNT to amount,
            KEY_EASY_PAY to easyPay,
        ).apply {
            if (customerSupportType.isNotBlank()) {
                put(CUSTOMER_SUPPORT_TYPE, customerSupportType)
                put(CUSTOMER_SUPPORT_NUMBER, customerSupportNumber)
                put(CUSTOMER_SUPPORT_MESSAGE, customerSupportMessage)
            }
        }

        analyticsProvider.get().trackEvents(PAYMENT_SUMMARY_PAGE_VIEWED, properties)
    }

    fun trackEnteredPaymentDetails(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        dueAmount: String,
        type: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            DUE_AMOUNT to dueAmount,
            TYPE to type,
        )

        analyticsProvider.get().trackEvents(ENTER_PAYMENT_DETAILS, properties)
    }

    fun trackClickPaymentRequestDetails(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        dueAmount: String,
        type: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            DUE_AMOUNT to dueAmount,
            TYPE to type,
        )

        analyticsProvider.get().trackEvents(CLICK_REQUEST_PAYMENT_DETAILS, properties)
    }

    fun trackChoosePaymentOption(
        accountId: String,
        relation: String,
        screen: String,
        flow: String,
        value: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            VALUE to value,
        )

        analyticsProvider.get().trackEvents(CHOOSE_PAYMENT_OPTION, properties)
    }

    fun trackConfirmPaymentDetails(
        accountId: String,
        relation: String,
        type: String,
        screen: String,
        flow: String,
        dueAmount: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            DUE_AMOUNT to dueAmount,
            TYPE to type
        )

        analyticsProvider.get().trackEvents(CONFIRM_PAYMENT_DETAILS, properties)
    }

    fun trackEnteredInvalidPaymentDetails(
        accountId: String,
        relation: String,
        type: String,
        screen: String,
        flow: String,
        error: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            TYPE to type,
            ERROR to error
        )

        analyticsProvider.get().trackEvents(ENTER_INVALID_PAYMENT_DETAILS, properties)
    }

    fun trackPaymentRewardStatusPageView(
        accountId: String,
        relation: String,
        screen: String,
        amount: String,
        paymentId: String,
        cashbackSeenStatus: String,
        rewardId: String,
        rewardValue: String,
        collectionStatus: Boolean,
        rewardStatus: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            AMOUNT to amount,
            PAYMENT_ID to paymentId,
            CASHBACK_SEEN_STATUS to cashbackSeenStatus,
            REWARD_ID to rewardId,
            REWARD_VALUE to rewardValue,
            COLLECTION_ADOPTED to collectionStatus,
            REWARD_STATUS to rewardStatus
        )
        analyticsProvider.get().trackEvents(PAYMENT_REWARD_STATUS_PAGE_VIEW, properties)
    }

    fun trackPaymentStatusRewardIconClicked(
        accountId: String,
        relation: String,
        screen: String,
        amount: String,
        paymentId: String,
        cashbackSeenStatus: String,
        rewardId: String,
        rewardValue: String,
        collectionStatus: Boolean,
        rewardStatus: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            AMOUNT to amount,
            PAYMENT_ID to paymentId,
            CASHBACK_SEEN_STATUS to cashbackSeenStatus,
            REWARD_ID to rewardId,
            REWARD_VALUE to rewardValue,
            COLLECTION_ADOPTED to collectionStatus,
            REWARD_STATUS to rewardStatus
        )
        analyticsProvider.get().trackEvents(PAYMENT_STATUS_REWARD_ICON_CLICKED, properties)
    }

    fun trackPaymentDetailsValidated(
        accountId: String,
        relation: String,
        type: String,
        screen: String,
        flow: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            FLOW to flow,
            TYPE to type
        )

        analyticsProvider.get().trackEvents(PAYMENT_DETAILS_VALIDATED, properties)
    }

    fun trackPaymentFlowApiError(
        accountId: String,
        msg: String,
        apiName: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            ERROR_MSG to msg,
            API_NAME to apiName
        )

        analyticsProvider.get().trackEvents(PAYMENT_FLOW_API_ERROR, properties)
    }

    fun trackCustomerSupportMsgClicked(
        source: String,
        type: String,
        txnId: String = "",
        amount: String = "",
        relation: String,
        status: String = "",
        supportMsg: String,
    ) {
        val properties = mapOf(
            SOURCE to source,
            TYPE_2 to type,
            TXN_ID to txnId,
            AMOUNT_2 to amount,
            RELATION to relation,
            STATUS to status,
            CUSTOMER_SUPPORT_MESSAGE to supportMsg,
        )

        analyticsProvider.get().trackEvents(CUSTOMER_SUPPORT_MSG_CLICKED, properties)
    }

    fun trackCustomerSupportOptMsgShown(
        accountId: String,
        supportType: String,
        msg: String = "",
        number: String = "",
        type: String,
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            CUSTOMER_SUPPORT_TYPE to supportType,
            CUSTOMER_SUPPORT_MESSAGE to msg,
            CUSTOMER_SUPPORT_NUMBER to number,
            TYPE_2 to type,
        )

        analyticsProvider.get().trackEvents(CUSTOMER_SUPPORT_OPT_MSG_VIEW, properties)
    }
}
