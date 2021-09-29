package tech.okcredit.help.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import javax.inject.Inject

class HelpEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    fun trackRuntimePermission(screen: String, type: String, granted: Boolean) {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.SCREEN] = screen
        properties[PropertyKey.TYPE] = type
        if (granted)
            analyticsProvider.get().trackEvents(Event.GRANT_PERMISSION, properties)
        else
            analyticsProvider.get().trackEvents(Event.DENY_PERMISSION, properties)
    }

    fun trackContactOkCredit(screen: String, type: String, source: String) {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.SCREEN] = screen
        properties[PropertyKey.TYPE] = type
        properties[PropertyKey.SOURCE] = source
        analyticsProvider.get().trackEvents(Event.CONTACT_OKCREDIT, properties)
    }

    fun trackWithEventName(eventName: String, screen: String? = null) {
        val properties = mutableMapOf<String, Any>()
        if (screen != null) properties[PropertyKey.SCREEN] = screen
        analyticsProvider.get().trackEvents(eventName, properties)
    }
}
