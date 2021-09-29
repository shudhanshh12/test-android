package `in`.okcredit.supplier.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.PayablesOnboardingProperty.CURRENT_DURATION
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.PayablesOnboardingProperty.POSITION
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.PayablesOnboardingProperty.SUPPLIER_CAROUSEL_TAB_VIEWED
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.PayablesOnboardingProperty.SUPPLIER_CAROUSEL_VIDEO_STATE_CHANGED
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.PayablesOnboardingProperty.TAB_TYPE
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.PayablesOnboardingProperty.VIDEO_DURATION
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.PayablesOnboardingProperty.VIDEO_ID
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.PayablesOnboardingProperty.VIDEO_STATE
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.ACCOUNT_ID
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.API_NAME
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.AVAILABLE_TRANSACTION_LIMIT
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.CASHBACK_MESSAGE_VISIBLE
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.DUE_AMOUNT
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.ERROR_MSG
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.FLOW
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.NUMBER_AVAILABLE
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.PAYMENT_DUE
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.RELATION
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.RISK_VALUE
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.SCREEN
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.TXN_TYPE
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.TYPE
import `in`.okcredit.supplier.analytics.SupplierAnalyticsEvents.SupplierProperty.USER_TRANSACTION_LIMIT
import dagger.Lazy
import javax.inject.Inject

class SupplierAnalyticsEvents @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        const val ONLINE_PAYMENT_CLICK = "Online Payment Click"
        const val PAYMENT_LIMIT_POP_DISPALYED = "Payment limit pop Displayed"
        const val PROFILE_ICON_CLICKED = "Profile Icon Clicked"
        const val PROFILE_POP_UP_DISPLAYED = "Profile Pop up displayed"
        const val SUPPLIER_TXN_PAGE_API_ERROR = "Supplier Txn Page Api Error"
    }

    object PayablesOnboardingProperty {
        const val SUPPLIER_CAROUSEL_TAB_VIEWED = "Supplier Carousel Tab Viewed"
        const val POSITION = "Position"
        const val TAB_TYPE = "Tab Type"
        const val VIDEO_ID = "Video Id"

        const val SUPPLIER_CAROUSEL_VIDEO_STATE_CHANGED = "Supplier Carousel Video State Changed"
        const val VIDEO_STATE = "Video State"
        const val CURRENT_DURATION = "Current Duration"
        const val VIDEO_DURATION = "Video Duration"
    }

    object SupplierProperty {
        const val ACCOUNT_ID = "account_id"
        const val DUE_AMOUNT = "Due Amount"
        const val SCREEN = "Screen"
        const val RELATION = "Relation"
        const val FLOW = "Flow"
        const val USER_TRANSACTION_LIMIT = "User Transaction Limit"
        const val AVAILABLE_TRANSACTION_LIMIT = "Available Transaction Limit"
        const val TYPE = "Type"
        const val TXN_TYPE = "Txn Type"
        const val NUMBER_AVAILABLE = "Number Available"
        const val PAYMENT_DUE = "Payment Due"
        const val ERROR_MSG = "Error Msg"
        const val API_NAME = "Api Name"
        const val RISK_VALUE = "Risk Value"
        const val CASHBACK_MESSAGE_VISIBLE = "Cashback Message visible"
        const val LAST_PAYMENT_AMOUNT = "last_payment_amount"
        const val LAST_PAYMENT_DATE = "last_payment_date"
        const val TOTAL_REMINDERS_LEFT = "total_reminders_left"
        const val ACTION = "action"
    }

    object SupplierPropertyValue {
        const val SUPPLIER_SCREEN = "Supplier Screen"
        const val CUSTOMER_SCREEN = "Customer Screen"
        const val PROFILE_POP_UP = "Profile PopUp"
        const val SUPPLIER = "Supplier"
        const val CUSTOMER = "Customer"
        const val JUSPAY_SUPPLIER_COLLECTION = "Juspay Supplier Collection"
        const val LIMIT_EXCEEDED = "Limit Exceeded"
        const val TXN_COUNT = "Txn count"
        const val TXN_AMOUNT = "Txn amount"
        const val RISK_API = "GetPaymentInstruments"
        const val GET_PAYOUT_LINK_DETAILS = "GetPaymentOutLinkDetail"
        const val PAY_NOW = "pay_now"
        const val DISMISS = "dismiss"
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
        txnType: String
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

    fun trackSupplierProfileIconClicked(
        accountId: String,
        relation: String,
        screen: String
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen
        )

        analyticsProvider.get().trackEvents(PROFILE_ICON_CLICKED, properties)
    }

    fun trackSupplierProfilePopUpDisplayed(
        accountId: String,
        relation: String,
        screen: String,
        numberAvailable: Boolean,
        paymentDue: Boolean
    ) {
        val properties = mapOf(
            ACCOUNT_ID to accountId,
            RELATION to relation,
            SCREEN to screen,
            NUMBER_AVAILABLE to numberAvailable,
            PAYMENT_DUE to paymentDue
        )

        analyticsProvider.get().trackEvents(PROFILE_POP_UP_DISPLAYED, properties)
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

    fun trackPayablesOnboardingCarouselTabSelected(position: Int, tabType: String, youtubeId: String = "") {
        val properties = mapOf(
            POSITION to position,
            TAB_TYPE to tabType,
            VIDEO_ID to youtubeId,
        )

        analyticsProvider.get().trackEvents(SUPPLIER_CAROUSEL_TAB_VIEWED, properties)
    }

    fun trackPayablesOnboardingCarouselVideoStateChanged(state: String, currentDuration: Int, videoDuration: Int) {
        val properties = mapOf(
            VIDEO_STATE to state,
            CURRENT_DURATION to currentDuration,
            VIDEO_DURATION to videoDuration,
        )

        analyticsProvider.get().trackEvents(SUPPLIER_CAROUSEL_VIDEO_STATE_CHANGED, properties)
    }
}
