package `in`.okcredit.communication_inappnotification.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyValue
import dagger.Lazy
import tech.okcredit.android.base.utils.getStringStackTrace
import javax.inject.Inject

class InAppNotificationTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>,
) {
    object Event {
        const val IN_APP_NOTIFICATION_ERROR = "InAppNotification:Error"
        const val IN_APP_NOTIFICATION_NOT_DISPLAYED = "InAppNotification:NotDisplayed"

        const val IN_APP_NOTIFICATION_RECEIVED = "InAppNotification Received"
        const val IN_APP_NOTIFICATION_DISPLAYED = "InAppNotification Displayed"
        const val IN_APP_NOTIFICATION_CLICKED = "InAppNotification Clicked"
        const val IN_APP_NOTIFICATION_CLEARED = "InAppNotification Cleared"
        const val IN_APP_NOTIFICATION_ACKNOWLEDGED = "InAppNotification Acknowledged"
    }

    object Key {
        const val TARGET_ID_TYPE = "TargetIdType"
        const val TARGET_ID = "TargetId"
        const val SCREEN_NAME = "ScreenName"

        const val TYPE = "Type"
        const val NOTIFICATION_ID = "NotificationId"
        const val NAME = "Name"
        const val SOURCE = "Source"
        const val VALUE = "Value"
    }

    fun track(event: String, properties: Map<String, String>) {
        analyticsProvider.get().trackEvents(event, properties)
    }

    fun trackNotificationDisplayError(
        exception: Exception,
        notificationId: String,
        targetIdType: String = "",
        targetId: String = "",
        type: String,
        screenName: String,
        name: String,
    ) {
        val properties = mapOf(
            Key.TARGET_ID_TYPE to targetIdType,
            Key.TARGET_ID to targetId,
            Key.NOTIFICATION_ID to notificationId,
            Key.TYPE to type,
            Key.NAME to name,
            Key.SCREEN_NAME to screenName,
            PropertyValue.REASON to (exception.message ?: ""),
            PropertyValue.CAUSE to (exception.cause?.message ?: ""),
            PropertyValue.STACKTRACE to exception.getStringStackTrace()
        )
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTIFICATION_ERROR, properties)
    }

    fun trackException(throwable: Throwable) {
        val properties = mapOf(
            PropertyValue.REASON to (throwable.message ?: ""),
            PropertyValue.CAUSE to (throwable.cause?.message ?: ""),
            PropertyValue.STACKTRACE to throwable.getStringStackTrace()
        )
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTIFICATION_ERROR, properties)
    }

    fun trackNotificationNotDisplayed(
        notificationId: String,
        type: String,
        screenName: String,
        reason: String,
        name: String,
    ) {
        val properties = mapOf(
            Key.NOTIFICATION_ID to notificationId,
            Key.TYPE to type,
            Key.NAME to name,
            Key.SCREEN_NAME to screenName,
            PropertyValue.REASON to reason
        )
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTIFICATION_NOT_DISPLAYED, properties)
    }

    fun trackNotificationReceived(
        type: String,
        id: String,
        name: String,
        source: String,
    ) {
        val properties = createPropertyMap(type, id, name, source)
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTIFICATION_RECEIVED, properties)
    }

    fun trackNotificationDisplayed(
        type: String,
        id: String,
        name: String,
        source: String,
    ) {
        val properties = createPropertyMap(type, id, name, source)
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTIFICATION_DISPLAYED, properties)
    }

    fun trackNotificationClicked(
        type: String,
        id: String,
        name: String,
        source: String,
        value: String,
    ) {
        val properties = mapOf(
            Key.TYPE to type,
            Key.NOTIFICATION_ID to id,
            Key.NAME to name,
            Key.SOURCE to source,
            Key.VALUE to value
        )
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTIFICATION_CLICKED, properties)
    }

    fun trackNotificationCleared(
        type: String,
        id: String,
        name: String,
        source: String,
    ) {
        val properties = createPropertyMap(type, id, name, source)
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTIFICATION_CLEARED, properties)
    }

    fun trackNotificationAcknowledged(
        type: String,
        id: String,
        name: String,
        source: String,
    ) {
        val properties = createPropertyMap(type, id, name, source)
        analyticsProvider.get().trackEvents(Event.IN_APP_NOTIFICATION_ACKNOWLEDGED, properties)
    }

    private fun createPropertyMap(type: String, id: String, name: String, source: String): Map<String, String> {
        return mapOf(
            Key.TYPE to type,
            Key.NOTIFICATION_ID to id,
            Key.NAME to name,
            Key.SOURCE to source
        )
    }
}
