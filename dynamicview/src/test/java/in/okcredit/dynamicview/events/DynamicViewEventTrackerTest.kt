package `in`.okcredit.dynamicview.events

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.dynamicview.data.model.Action
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class DynamicViewEventTrackerTest {

    @Test
    fun `should call track method with event and properties`() {
        // Given
        val analyticsProvider: AnalyticsProvider = mock()
        val tracker: DynamicViewEventTracker = DynamicViewEventTracker(analyticsProvider)

        // When
        tracker.track(Action.Track("event_name", mapOf("merchant" to "OkCredit")))

        // Then
        verify(analyticsProvider).trackEvents("event_name", mapOf("merchant" to "OkCredit"))
    }
}
