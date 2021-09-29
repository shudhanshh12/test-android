package `in`.okcredit.communication_inappnotification.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class InAppNotificationTrackerTest {

    private val analyticsProvider: AnalyticsProvider = mock()
    private val inAppNotificationTracker = InAppNotificationTracker { analyticsProvider }

    @Test
    fun `track should call trackEvents on analyticsProvider`() {
        // given
        val event = "event-name"
        val properties = mapOf("key-1" to "val-1", "key-2" to "val-2")

        // when
        inAppNotificationTracker.track(event, properties)

        // then
        verify(analyticsProvider).trackEvents(event, properties)
    }

    @Test
    fun `trackNotificationNotDisplayed should call trackEvents with event IN_APP_NOTIFICATION_NOT_DISPLAYED on analyticsProvider`() {
        // given
        val event = InAppNotificationTracker.Event.IN_APP_NOTIFICATION_NOT_DISPLAYED
        val notificationId = "notificationId"
        val type = "type"
        val screenName = "screen-name"
        val reason = "reason"
        val name = "name"
        val properties = mapOf(
            InAppNotificationTracker.Key.NOTIFICATION_ID to notificationId,
            InAppNotificationTracker.Key.TYPE to type,
            InAppNotificationTracker.Key.SCREEN_NAME to screenName,
            PropertyValue.REASON to reason,
            InAppNotificationTracker.Key.NAME to name
        )

        // when
        inAppNotificationTracker.trackNotificationNotDisplayed(notificationId, type, screenName, reason, name)

        // then
        verify(analyticsProvider).trackEvents(event, properties)
    }

    @Test
    fun `trackNotificationReceived should call trackEvents with event IN_APP_NOTIFICATION_RECEIVED on analyticsProvider`() {
        // given
        val event = InAppNotificationTracker.Event.IN_APP_NOTIFICATION_RECEIVED
        val id = "notificationId"
        val type = "type"
        val name = "name"
        val source = "source"
        val properties = mapOf(
            InAppNotificationTracker.Key.TYPE to type,
            InAppNotificationTracker.Key.NOTIFICATION_ID to id,
            InAppNotificationTracker.Key.NAME to name,
            InAppNotificationTracker.Key.SOURCE to source
        )

        // when
        inAppNotificationTracker.trackNotificationReceived(type, id, name, source)

        // then
        verify(analyticsProvider).trackEvents(event, properties)
    }

    @Test
    fun `trackNotificationDisplayed should call trackEvents with event IN_APP_NOTIFICATION_DISPLAYED on analyticsProvider`() {
        // given
        val event = InAppNotificationTracker.Event.IN_APP_NOTIFICATION_DISPLAYED
        val id = "notificationId"
        val type = "type"
        val name = "name"
        val source = "source"
        val properties = mapOf(
            InAppNotificationTracker.Key.TYPE to type,
            InAppNotificationTracker.Key.NOTIFICATION_ID to id,
            InAppNotificationTracker.Key.NAME to name,
            InAppNotificationTracker.Key.SOURCE to source,
        )

        // when
        inAppNotificationTracker.trackNotificationDisplayed(type, id, name, source)

        // then
        verify(analyticsProvider).trackEvents(event, properties)
    }

    @Test
    fun `trackNotificationClicked should call trackEvents with event IN_APP_NOTIFICATION_CLICKED on analyticsProvider`() {
        // given
        val event = InAppNotificationTracker.Event.IN_APP_NOTIFICATION_CLICKED
        val id = "notificationId"
        val type = "type"
        val name = "name"
        val source = "source"
        val value = "value"
        val properties = mapOf(
            InAppNotificationTracker.Key.TYPE to type,
            InAppNotificationTracker.Key.NOTIFICATION_ID to id,
            InAppNotificationTracker.Key.NAME to name,
            InAppNotificationTracker.Key.SOURCE to source,
            InAppNotificationTracker.Key.VALUE to value
        )

        // when
        inAppNotificationTracker.trackNotificationClicked(type, id, name, source, value)

        // then
        verify(analyticsProvider).trackEvents(event, properties)
    }

    @Test
    fun `trackNotificationReceived should call trackEvents with event IN_APP_NOTIFICATION_CLEARED on analyticsProvider`() {
        // given
        val event = InAppNotificationTracker.Event.IN_APP_NOTIFICATION_CLEARED
        val id = "notificationId"
        val type = "type"
        val name = "name"
        val source = "source"
        val properties = mapOf(
            InAppNotificationTracker.Key.TYPE to type,
            InAppNotificationTracker.Key.NOTIFICATION_ID to id,
            InAppNotificationTracker.Key.NAME to name,
            InAppNotificationTracker.Key.SOURCE to source
        )

        // when
        inAppNotificationTracker.trackNotificationCleared(type, id, name, source)

        // then
        verify(analyticsProvider).trackEvents(event, properties)
    }

    @Test
    fun `trackNotificationReceived should call trackEvents with event IN_APP_NOTIFICATION_ACKNOWLEDGED on analyticsProvider`() {
        // given
        val event = InAppNotificationTracker.Event.IN_APP_NOTIFICATION_ACKNOWLEDGED
        val id = "notificationId"
        val type = "type"
        val name = "name"
        val source = "source"
        val properties = mapOf(
            InAppNotificationTracker.Key.TYPE to type,
            InAppNotificationTracker.Key.NOTIFICATION_ID to id,
            InAppNotificationTracker.Key.NAME to name,
            InAppNotificationTracker.Key.SOURCE to source
        )

        // when
        inAppNotificationTracker.trackNotificationAcknowledged(type, id, name, source)

        // then
        verify(analyticsProvider).trackEvents(event, properties)
    }
}
