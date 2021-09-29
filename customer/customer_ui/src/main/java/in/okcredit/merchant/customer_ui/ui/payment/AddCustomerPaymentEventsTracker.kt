package `in`.okcredit.merchant.customer_ui.ui.payment

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import javax.inject.Inject

class AddCustomerPaymentEventsTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Events {
        const val VIEW_QR = "View QR"
        const val SHOW_CUSTOMER_QR = "show_customer_qr"
        const val CUSTOMER_QR_MINIMIZED = "customer_qr_minimized"
        const val PAYMENT_TRANSACTION_SCREEN_SHOWN = "payment_transaction_screen_shown"
    }

    object Params {
        const val ACCOUNT_ID = "account_id"
        const val TYPE = "type"
        const val AMOUNT = "amount"
        const val TRANSACTION_ID = "transaction_id"
    }

    companion object {
        const val CUSTOMER_QR_SCREEN = "add_transaction_customer_qr"
    }

    fun trackViewQr(screen: String) {
        val eventProperties = mapOf<String, Any>(
            PropertyKey.SCREEN to screen,
        )
        analyticsProvider.get().trackEvents(Events.VIEW_QR, eventProperties)
    }

    fun trackShowCustomerQr(
        accountId: String,
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(Events.SHOW_CUSTOMER_QR, eventProperties)
    }

    fun trackCustomerQrMinimized(
        accountId: String,
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.ACCOUNT_ID to accountId,
        )
        analyticsProvider.get().trackEvents(Events.CUSTOMER_QR_MINIMIZED, eventProperties)
    }

    fun trackPaymentSuccessScreenShown(
        accountId: String,
        amount: Long,
        transactionId: String,
    ) {
        val eventProperties = mapOf<String, Any>(
            Params.ACCOUNT_ID to accountId,
            Params.AMOUNT to amount,
            Params.TRANSACTION_ID to transactionId,
        )
        analyticsProvider.get().trackEvents(Events.PAYMENT_TRANSACTION_SCREEN_SHOWN, eventProperties)
    }
}
