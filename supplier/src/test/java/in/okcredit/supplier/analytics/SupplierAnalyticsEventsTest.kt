package `in`.okcredit.supplier.analytics

import `in`.okcredit.analytics.AnalyticsProvider
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
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class SupplierAnalyticsEventsTest {

    private val analyticsProvider: AnalyticsProvider = mock()

    private val supplierAnalyticsEvents = SupplierAnalyticsEvents { analyticsProvider }

    @Test
    fun `trackSupplierOnlinePaymentClick() should call trackEvents `() {
        val accountId = "accountId"
        val mobile = "mobile"
        val dueAmount = "dueAmount"
        val screen = "screen"
        val relation = "relation"
        val riskType = "riskType"
        val isCashbackMessageVisible = false

        supplierAnalyticsEvents.trackSupplierOnlinePaymentClick(
            accountId,
            dueAmount,
            screen,
            relation,
            riskType,
            isCashbackMessageVisible,
        )

        verify(analyticsProvider).trackEvents(
            "Online Payment Click",
            mapOf(
                ACCOUNT_ID to accountId,
                DUE_AMOUNT to dueAmount,
                SCREEN to screen,
                RELATION to relation,
                RISK_VALUE to riskType,
                CASHBACK_MESSAGE_VISIBLE to isCashbackMessageVisible,
            )
        )
    }

    @Test
    fun `trackSupplierPaymentLimitPopDisplayed() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val flow = "flow"
        val dueAmount = "dueAmount"
        val type = "type"
        val userTxnLimit = "userTxnLimit"
        val availTxnLimit = "availTxnLimit"
        val txnType = "txnType"

        supplierAnalyticsEvents.trackSupplierPaymentLimitPopDisplayed(
            accountId,
            relation, screen, flow, dueAmount, type, userTxnLimit, availTxnLimit, txnType
        )

        verify(analyticsProvider).trackEvents(
            "Payment limit pop Displayed",
            mapOf(
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
        )
    }

    @Test
    fun `trackSupplierProfileIconClicked() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"

        supplierAnalyticsEvents.trackSupplierProfileIconClicked(
            accountId,
            relation, screen
        )

        verify(analyticsProvider).trackEvents(
            "Profile Icon Clicked",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen
            )
        )
    }

    @Test
    fun `trackSupplierProfilePopUpDisplayed() should call trackEvents `() {
        val accountId = "accountId"
        val relation = "relation"
        val screen = "screen"
        val numberAvailable = false
        val paymentDue = false

        supplierAnalyticsEvents.trackSupplierProfilePopUpDisplayed(
            accountId,
            relation, screen, numberAvailable, paymentDue
        )

        verify(analyticsProvider).trackEvents(
            "Profile Pop up displayed",
            mapOf(
                ACCOUNT_ID to accountId,
                RELATION to relation,
                SCREEN to screen,
                NUMBER_AVAILABLE to numberAvailable,
                PAYMENT_DUE to paymentDue
            )
        )
    }

    @Test
    fun `trackSupplierTxnPageApiError() should call trackEvents `() {
        val accountId = "accountId"
        val msg = "msg"
        val apiName = "apiName"
        val screen = "screen"

        supplierAnalyticsEvents.trackSupplierTxnPageApiError(
            accountId,
            msg, apiName, screen
        )

        verify(analyticsProvider).trackEvents(
            "Supplier Txn Page Api Error",
            mapOf(
                ACCOUNT_ID to accountId,
                ERROR_MSG to msg,
                API_NAME to apiName,
                SCREEN to screen
            )
        )
    }
}
