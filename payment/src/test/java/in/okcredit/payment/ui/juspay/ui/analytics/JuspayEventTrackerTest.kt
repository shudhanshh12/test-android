package `in`.okcredit.payment.ui.juspay.ui.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.payment.ui.juspay.analytics.JuspayEventTracker
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class JuspayEventTrackerTest {
    private val analyticsProvider: AnalyticsProvider = mock()
    private val juspayEventTracker = JuspayEventTracker { analyticsProvider }

    @Test
    fun `trackEventJuspayAmountPageLoaded should call event`() {

        juspayEventTracker.trackEventJuspayAmountPageLoaded()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: loaded_amount_page"
        )
    }

    @Test
    fun `trackEventJuspayClickedProceed should call event`() {

        juspayEventTracker.trackEventJuspayClickedProceed("payment_id")

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: clicked_proceed",
            properties = mapOf("PaymentId" to "payment_id")
        )
    }

    @Test
    fun `trackEventJuspayEditedAmount should call event`() {

        juspayEventTracker.trackEventJuspayEditedAmount()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: edited_amount"
        )
    }

    @Test
    fun `trackEventJuspayLoadedSuccessPage should call event`() {

        juspayEventTracker.trackEventJuspayLoadedSuccessPage()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: loaded_success_page"
        )
    }

    @Test
    fun `trackEventJuspayLoadedFailure should call event`() {

        juspayEventTracker.trackEventJuspayLoadedFailure()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: loaded_failure_page"
        )
    }

    @Test
    fun `trackEventJuspayLoadedPendingPage should call event`() {

        juspayEventTracker.trackEventJuspayLoadedPendingPage()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: loaded_pending_page"
        )
    }

    @Test
    fun `trackEventJuspayPrefetchCalled should call event`() {

        juspayEventTracker.trackEventJuspayPrefetchCalled()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: prefetch called"
        )
    }

    @Test
    fun `trackEventJuspayInitiateCalled should call event`() {

        juspayEventTracker.trackEventJuspayInitiateCalled("juspay_screen")

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: initiate called",
            properties = mapOf("Screen" to "juspay_screen")
        )
    }

    @Test
    fun `trackEventJuspayProcessCalled should call event`() {

        juspayEventTracker.trackEventJuspayProcessCalled()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: process called"
        )
    }

    @Test
    fun `trackEventJuspayInitiateResultCalled should call event `() {

        juspayEventTracker.trackEventJuspayInitiateResultCalled()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: initiate_result called"
        )
    }

    @Test
    fun `trackEventJuspayProcessResultCalled should call event with properties`() {

        juspayEventTracker.trackEventJuspayProcessResultCalled("type_initiate", "order_id")

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: process_result called",
            properties = mapOf("Type" to "type_initiate", "OrderId" to "order_id")
        )
    }

    @Test
    fun `trackEventJuspayHideLoaderCalled should call event`() {

        juspayEventTracker.trackEventJuspayHideLoaderCalled()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: hide_loader called"
        )
    }

    @Test
    fun `trackEventJuspayBackPressControl should call event`() {

        juspayEventTracker.trackEventJuspayBackPressControl()

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: back press control given to app"
        )
    }

    @Test
    fun `trackEventJuspayActivityLifeCycle should call event`() {

        juspayEventTracker.trackEventJuspayActivityLifeCycle("lifecycle_type")

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay: activity life cycle",
            properties = mapOf("Type" to "lifecycle_type")
        )
    }

    @Test
    fun `trackEventJuspayProcessError should call event`() {

        juspayEventTracker.trackEventJuspayProcessError("payload_status", "message_string", "error_code")

        verify(analyticsProvider).trackEvents(
            eventName = "Juspay sdk error",
            properties = mapOf(
                "message" to "message_string",
                "Payload Status" to "payload_status",
                "Error Code" to "error_code"
            )
        )
    }
}
