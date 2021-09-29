package `in`.okcredit.payment.ui.payment_destination.analytics

import `in`.okcredit.analytics.IAnalyticsProvider
import dagger.Lazy
import javax.inject.Inject

class PaymentDestinationAdoptionTracker @Inject constructor(private val analyticsProvider: Lazy<IAnalyticsProvider>) {

    companion object {
        private const val PAYMENT_DESTINATION_DIALOG_LOADED = "Payment Destination: dialog loaded"
        private const val VALIDATE_BUTTON_CLICKED = "Payment Destination: validate button clicked"
        private const val SWITCH_BUTTON_CLICKED = "Payment Destination: switch option button clicked"
        private const val SCAN_BUTTON_CLICKED = "Payment Destination: scan button clicked"
        private const val UPI_PROVIDED_BY_SCAN = "Payment Destination: upi provided by scan"
        private const val ERROR_SHOWN = "Payment Destination: error"

        private const val SWITCH_TO = "switch_to"
        private const val ERROR_MSG = "error_msg"
    }

    fun trackEventPaymentDestinationDialogLoaded() {
        analyticsProvider.get().trackEvents(PAYMENT_DESTINATION_DIALOG_LOADED, null)
    }

    fun trackEventValidateBtnClicked() {
        analyticsProvider.get().trackEvents(VALIDATE_BUTTON_CLICKED, null)
    }

    fun trackEventSwitchOptionBtnClicked(switchTo: String) {
        val properties = mapOf(SWITCH_TO to switchTo)
        analyticsProvider.get().trackEvents(SWITCH_BUTTON_CLICKED, properties)
    }

    fun trackEventScanBtnClicked() {
        analyticsProvider.get().trackEvents(SCAN_BUTTON_CLICKED, null)
    }

    fun trackEventUpiProvidedByScan() {
        analyticsProvider.get().trackEvents(UPI_PROVIDED_BY_SCAN, null)
    }

    fun trackEventErrorShown(errMsg: String) {
        val properties = mapOf(ERROR_MSG to errMsg)
        analyticsProvider.get().trackEvents(ERROR_SHOWN, properties)
    }
}
