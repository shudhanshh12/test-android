package `in`.okcredit.payment.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey.COLLECTION_ADOPTED
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.Companion.AMOUNT_ENTERED
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.ACCOUNT_ID
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.ACTION
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.ADDRESS
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.AMOUNT
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.API_NAME
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.AVAILABLE_TRANSACTION_LIMIT
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
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.TYPE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.USER_TRANSACTION_LIMIT
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentProperty.VALUE
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.JUSPAY_SUPPLIER_COLLECTION
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.KEY_EASY_PAY
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.PAYMENT_SUMMARY_SCREEN
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class PaymentAnalyticsEventsTest {
    private val analyticsProvider: AnalyticsProvider = mock()

    private val paymentAnalyticsEvents = PaymentAnalyticsEvents { analyticsProvider }

    @Test
    fun `trackPaymentAmountEntered() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val dueAmount = "dueAmount"
        val userTxnLimit = "mobile"
        val availTxnLimit = "mobile"
        val limitExhausted = false
        val amount = "amount"
        val preFilledAmount = "preFilledAmount"
        val riskValue = "riskValue"
        val easyPay = false

        paymentAnalyticsEvents.trackPaymentAmountEntered(
            accountId,
            relation,
            screen,
            flow,
            dueAmount,
            userTxnLimit,
            availTxnLimit,
            false,
            amount,
            preFilledAmount,
            riskValue
        )

        verify(analyticsProvider).trackEvents(
            "Amount Entered",
            mapOf(
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
                PaymentAnalyticsEvents.PaymentPropertyValue.KEY_EASY_PAY to easyPay
            )
        )
    }

    @Test
    fun `trackPaymentLimitWarningDisplayed() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val dueAmount = "dueAmount"
        val userTxnLimit = "mobile"
        val availTxnLimit = "mobile"
        val amount = "amount"
        val preFilledAmount = "preFilledAmount"
        val easyPay = false

        paymentAnalyticsEvents.trackPaymentLimitWarningDisplayed(
            accountId,
            relation,
            screen,
            flow,
            dueAmount,
            userTxnLimit,
            availTxnLimit,
            amount,
            preFilledAmount
        )

        verify(analyticsProvider).trackEvents(
            "Payment limit Warning Displayed",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                DUE_AMOUNT to dueAmount,
                USER_TRANSACTION_LIMIT to userTxnLimit,
                AVAILABLE_TRANSACTION_LIMIT to availTxnLimit,
                AMOUNT_ENTERED to amount,
                PRE_FILLED_AMOUNT to preFilledAmount,
                PaymentAnalyticsEvents.PaymentPropertyValue.KEY_EASY_PAY to easyPay
            )
        )
    }

    @Test
    fun `trackChangePaymentDetails() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val dueAmount = "dueAmount"
        val type = "type"

        paymentAnalyticsEvents.trackChangePaymentDetails(
            accountId,
            relation,
            screen,
            flow,
            dueAmount,
            type,
        )

        verify(analyticsProvider).trackEvents(
            "Change Payment Details",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                DUE_AMOUNT to dueAmount,
                TYPE to type,
            )
        )
    }

    @Test
    fun `trackPaymentStatusWaitingPage() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val easyPay = false

        paymentAnalyticsEvents.trackPaymentStatusWaitingPage(
            accountId,
            relation,
            screen,
            flow,
        )

        verify(analyticsProvider).trackEvents(
            "payment Status Waiting page",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                PaymentAnalyticsEvents.PaymentPropertyValue.KEY_EASY_PAY to easyPay
            )
        )
    }

    @Test
    fun `trackPaymentStatusPageView() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val type = "type"
        val screen = "screen"
        val flow = "flow"
        val amount = "amount"
        val paymentId = "paymentId"
        val address = "address"
        val provider = "provider"
        val status = "status"
        val timeTaken = "timeTaken"
        val collectionStatus = false
        val easyPay = false
        val customerSupportType = ""
        val customerSupportNumber = ""
        val customerSupportMessage = ""

        paymentAnalyticsEvents.trackPaymentStatusPageView(
            accountId,
            relation,
            type,
            screen,
            flow,
            amount,
            paymentId,
            address,
            provider,
            status,
            timeTaken,
            collectionStatus,
            easyPay,
            customerSupportType,
            customerSupportNumber,
            customerSupportMessage,
        )

        verify(analyticsProvider).trackEvents(
            "Payment Status Page View",
            mapOf(
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
            )
        )
    }

    @Test
    fun `trackPaymentStatusClick() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val action = "action"
        val easyPay = false
        val status = "status"

        paymentAnalyticsEvents.trackPaymentStatusClick(
            accountId,
            relation,
            screen,
            flow,
            action,
            easyPay,
            status,
        )

        verify(analyticsProvider).trackEvents(
            "Payment Status click",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                ACTION to action,
                KEY_EASY_PAY to easyPay,
                STATUS to status
            )
        )
    }

    @Test
    fun `trackLoadedPaymentErrorPage() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val type = "type"

        paymentAnalyticsEvents.trackLoadedPaymentErrorPage(
            accountId,
            relation,
            type,
            screen,
            flow

        )

        verify(analyticsProvider).trackEvents(
            "Loaded Payment Error page",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                TYPE to type
            )
        )
    }

    @Test
    fun `trackClickedRetryPayment() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val type = "type"

        paymentAnalyticsEvents.trackClickedRetryPayment(
            accountId,
            relation,
            type,
            screen,
            flow
        )

        verify(analyticsProvider).trackEvents(
            "Click Retry Payment",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                TYPE to type
            )
        )
    }

    @Test
    fun `trackClickProceedPayment() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val dueAmount = "dueAmount"
        val amount = "amount"
        val type = "type"
        val riskType = "riskType"
        val easyPay = false

        paymentAnalyticsEvents.trackClickProceedPayment(
            accountId,
            relation,
            screen,
            dueAmount,
            amount,
            type,
            riskType,
        )

        verify(analyticsProvider).trackEvents(
            "Click Proceed Payment",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                DUE_AMOUNT to dueAmount,
                AMOUNT to amount,
                TYPE to type,
                RISK_VALUE to riskType,
                PaymentAnalyticsEvents.PaymentPropertyValue.KEY_EASY_PAY to easyPay
            )
        )
    }

    @Test
    fun `trackPaymentSummaryPageViewed() should call trackEvents `() {
        val accountId = "accountId"
        val dueAmount = "dueAmount"
        val userTxnLimit = "userTxnLimit"
        val availTxnLimit = "availTxnLimit"
        val limitExhausted = false
        val preFilledAmount = "preFilledAmount"
        val riskValue = "riskValue"
        val amount = "amount"
        val relation = "relation"
        val easyPay = false
        val customerSupportType = "call"
        val customerSupportNumber = "999999999"
        val customerSupportMessage = "Hello , I need help"

        paymentAnalyticsEvents.trackPaymentSummaryPageViewed(
            accountId,
            dueAmount,
            userTxnLimit,
            availTxnLimit,
            limitExhausted,
            preFilledAmount,
            riskValue,
            amount,
            relation,
            easyPay,
            customerSupportType,
            customerSupportNumber,
            customerSupportMessage
        )

        verify(analyticsProvider).trackEvents(
            "Enter Amount Page view",
            mapOf(
                ACCOUNT_ID to accountId,
                DUE_AMOUNT to dueAmount,
                USER_TRANSACTION_LIMIT to userTxnLimit,
                AVAILABLE_TRANSACTION_LIMIT to availTxnLimit,
                LIMIT_EXHAUSTED to limitExhausted,
                PRE_FILLED_AMOUNT to preFilledAmount,
                RISK_VALUE to riskValue,
                AMOUNT to amount,
                RELATION to relation,
                SCREEN to PAYMENT_SUMMARY_SCREEN,
                FLOW to JUSPAY_SUPPLIER_COLLECTION,
                KEY_EASY_PAY to easyPay,
                CUSTOMER_SUPPORT_TYPE to customerSupportType,
                CUSTOMER_SUPPORT_NUMBER to customerSupportNumber,
                CUSTOMER_SUPPORT_MESSAGE to customerSupportMessage,
            )
        )
    }

    @Test
    fun `trackEnteredPaymentDetails() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val dueAmount = "dueAmount"
        val type = "type"

        paymentAnalyticsEvents.trackEnteredPaymentDetails(
            accountId,
            relation,
            screen,
            flow,
            dueAmount,
            type,
        )

        verify(analyticsProvider).trackEvents(
            "Enter Payment Details",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                DUE_AMOUNT to dueAmount,
                TYPE to type,
            )
        )
    }

    @Test
    fun `trackClickPaymentRequestDetails() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val dueAmount = "dueAmount"
        val type = "type"

        paymentAnalyticsEvents.trackClickPaymentRequestDetails(
            accountId,
            relation,
            screen,
            flow,
            dueAmount,
            type
        )

        verify(analyticsProvider).trackEvents(
            "Click Request payment Details",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                DUE_AMOUNT to dueAmount,
                TYPE to type,
            )
        )
    }

    @Test
    fun `trackChoosePaymentOption() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val value = "value"

        paymentAnalyticsEvents.trackChoosePaymentOption(
            accountId,
            relation,
            screen,
            flow,
            value,
        )

        verify(analyticsProvider).trackEvents(
            "Choose payment option",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                FLOW to flow,
                VALUE to value,
            )
        )
    }

    @Test
    fun `trackConfirmPaymentDetails() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val type = "type"
        val screen = "screen"
        val flow = "flow"
        val dueAmount = "dueAmount"

        paymentAnalyticsEvents.trackConfirmPaymentDetails(
            accountId,
            relation,
            type,
            screen,
            flow,
            dueAmount
        )

        verify(analyticsProvider).trackEvents(
            "Confirm Payment Details",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                TYPE to type,
                SCREEN to screen,
                FLOW to flow,
                DUE_AMOUNT to dueAmount
            )
        )
    }

    @Test
    fun `trackEnteredInvalidPaymentDetails() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val type = "type"
        val screen = "screen"
        val flow = "flow"
        val error = "error"

        paymentAnalyticsEvents.trackEnteredInvalidPaymentDetails(
            accountId,
            relation,
            type,
            screen,
            flow,
            error,
        )

        verify(analyticsProvider).trackEvents(
            "Entered Invalid Payment Details",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                TYPE to type,
                SCREEN to screen,
                FLOW to flow,
                ERROR to error,
            )
        )
    }

    @Test
    fun `trackPaymentDetailsValidated() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val type = "type"
        val screen = "screen"
        val flow = "flow"

        paymentAnalyticsEvents.trackPaymentDetailsValidated(
            accountId,
            relation,
            type,
            screen,
            flow,
        )

        verify(analyticsProvider).trackEvents(
            "Payment Details Validated",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                TYPE to type,
                SCREEN to screen,
                FLOW to flow,
            )
        )
    }

    @Test
    fun `trackPaymentFlowApiError() should call trackEvents `() {
        val accountId = "accountId"
        val msg = "msg"
        val apiName = "apiName"

        paymentAnalyticsEvents.trackPaymentFlowApiError(
            accountId,
            msg,
            apiName
        )

        verify(analyticsProvider).trackEvents(
            "Payment Flow Api Error",
            mapOf(
                ACCOUNT_ID to accountId,
                ERROR_MSG to msg,
                API_NAME to apiName
            )
        )
    }
}
