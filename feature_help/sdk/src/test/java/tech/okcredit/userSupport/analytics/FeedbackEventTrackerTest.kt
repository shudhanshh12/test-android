package tech.okcredit.userSupport.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class FeedbackEventTrackerTest {
    private val ab: AnalyticsProvider = mock()
    private val feedbackEventTracker = FeedbackEventTracker { ab }

    @Test
    fun `should call track event with correct name when trackViewChat is called`() {
        feedbackEventTracker.trackViewChat(
            PropertyValue.DRAWER,
            tech.okcredit.userSupport.analytics.FeedbackEventTracker.FEEDBACK_INTERACTION_SUBMIT
        )

        verify(ab).trackEvents(
            "View Feedback",
            mapOf(
                "Interaction" to "Submited",
                "Source" to "Drawer"
            )
        )
    }
}
