package `in`.okcredit.payment.ui.juspay.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import dagger.Lazy
import javax.inject.Inject

class JuspayEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        private const val JUSPAY_LOADED_AMOUNT_PAGE = "Juspay: loaded_amount_page"
        private const val JUSPAY_CLICKED_PROCEED = "Juspay: clicked_proceed"
        private const val JUSPAY_EDITED_AMOUNT = "Juspay: edited_amount"
        private const val JUSPAY_LOADED_SUCCESS_PAGE = "Juspay: loaded_success_page"
        private const val JUSPAY_LOADED_FAILURE_PAGE = "Juspay: loaded_failure_page"
        private const val JUSPAY_LOADED_PENDING_PAGE = "Juspay: loaded_pending_page"
        private const val JUSPAY_PREFETCH_CALLED = "Juspay: prefetch called"
        private const val JUSPAY_INITIATE_CALLED = "Juspay: initiate called"
        private const val JUSPAY_PROCESS_CALLED = "Juspay: process called"
        private const val JUSPAY_INITIATE_RESULT_CALLED = "Juspay: initiate_result called"
        private const val JUSPAY_PROCESS_RESULT_CALLED = "Juspay: process_result called"
        private const val JUSPAY_BACK_PRESS_CONTROL = "Juspay: back press control given to app"
        private const val JUSPAY_HIDE_LOADER_CALLED = "Juspay: hide_loader called"
        private const val JUSPAY_ACTIVITY_LIFE_CYCLE = "Juspay: activity life cycle"
        private const val JUSPAY_SDK_ERROR = "Juspay sdk error"

        private const val SCREEN = "Screen"
        private const val TYPE = "Type"
        private const val MESSAGE = "message"
        private const val PAYMENT_ID = "PaymentId"
        private const val ORDER_ID = "OrderId"
        const val WORKER_FRAGMENT = "worker fragment"
        private const val PAYLOAD_STATUS = "Payload Status"
        private const val ERROR_CODE = "Error Code"
    }

    fun trackEventJuspayAmountPageLoaded() {
        analyticsProvider.get().trackEvents(JUSPAY_LOADED_AMOUNT_PAGE, null)
    }

    fun trackEventJuspayClickedProceed(paymentId: String) {
        val properties = mapOf(PAYMENT_ID to paymentId)
        analyticsProvider.get().trackEvents(JUSPAY_CLICKED_PROCEED, properties)
    }

    fun trackEventJuspayEditedAmount() {
        analyticsProvider.get().trackEvents(JUSPAY_EDITED_AMOUNT, null)
    }

    fun trackEventJuspayLoadedSuccessPage() {
        analyticsProvider.get().trackEvents(JUSPAY_LOADED_SUCCESS_PAGE, null)
    }

    fun trackEventJuspayLoadedFailure() {
        analyticsProvider.get().trackEvents(JUSPAY_LOADED_FAILURE_PAGE, null)
    }

    fun trackEventJuspayLoadedPendingPage() {
        analyticsProvider.get().trackEvents(JUSPAY_LOADED_PENDING_PAGE, null)
    }

    fun trackEventJuspayPrefetchCalled() {
        analyticsProvider.get().trackEvents(JUSPAY_PREFETCH_CALLED, null)
    }

    fun trackEventJuspayInitiateCalled(screen: String) {
        val properties = mapOf(SCREEN to screen)
        analyticsProvider.get().trackEvents(JUSPAY_INITIATE_CALLED, properties)
    }

    fun trackEventJuspayProcessCalled() {
        analyticsProvider.get().trackEvents(JUSPAY_PROCESS_CALLED, null)
    }

    fun trackEventJuspayInitiateResultCalled() {
        analyticsProvider.get().trackEvents(JUSPAY_INITIATE_RESULT_CALLED, null)
    }

    fun trackEventJuspayProcessResultCalled(type: String, orderId: String) {
        val properties = mapOf(TYPE to type, ORDER_ID to orderId)
        analyticsProvider.get().trackEvents(JUSPAY_PROCESS_RESULT_CALLED, properties)
    }

    fun trackEventJuspayHideLoaderCalled() {
        analyticsProvider.get().trackEvents(JUSPAY_HIDE_LOADER_CALLED, null)
    }

    fun trackEventJuspayBackPressControl() {
        analyticsProvider.get().trackEvents(JUSPAY_BACK_PRESS_CONTROL, null)
    }

    fun trackEventJuspayActivityLifeCycle(type: String) {
        val properties = mapOf(TYPE to type)
        analyticsProvider.get().trackEvents(JUSPAY_ACTIVITY_LIFE_CYCLE, properties)
    }

    fun trackEventJuspayProcessError(payloadStatus: String, errorMessage: String, errorCode: String) {
        val properties = mapOf(MESSAGE to errorMessage, PAYLOAD_STATUS to payloadStatus, ERROR_CODE to errorCode)
        analyticsProvider.get().trackEvents(JUSPAY_SDK_ERROR, properties)
    }
}
