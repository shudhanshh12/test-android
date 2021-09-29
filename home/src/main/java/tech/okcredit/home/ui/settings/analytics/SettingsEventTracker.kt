package tech.okcredit.home.ui.settings.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import tech.okcredit.contract.Constants
import tech.okcredit.contract.UPDATE_PIN
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.PASSWORD_CHANGE_CLICKED
import tech.okcredit.home.ui.settings.analytics.SettingsEventProperties.SECURITY
import javax.inject.Inject

class SettingsEventTracker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>
) {

    fun trackViewProfile(screen: String, type: String, mobile: String? = null) {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.SCREEN] = screen
        properties[PropertyKey.TYPE] = type
        analyticsProvider.get().trackEvents(Event.VIEW_PROFILE, properties)
    }

    fun trackViewLanguage(screen: String, type: String, list: String) {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.SCREEN] = screen
        properties[PropertyKey.TYPE] = type
        properties[PropertyKey.LIST] = list
        analyticsProvider.get().trackEvents(Event.VIEW_LANGUAGE, properties)
    }

    fun trackSecurityScreenEvents(eventName: String, screen: String? = null, flow: String? = null) {
        val properties = mutableMapOf<String, Any>()
        if (screen != null) properties[PropertyKey.SCREEN] = screen
        if (flow != null) properties[PropertyKey.FLOW] = flow
        analyticsProvider.get().trackEvents(eventName, properties)
    }

    fun trackPinSet() {
        val properties = mapOf(
            PropertyKey.FLOW to SettingsEventProperties.SETTINGS_PAGE
        )
        analyticsProvider.get().trackEvents(Constants.SECURITY_PIN_SET, properties)
    }

    fun trackPinChanged() {
        val properties = mapOf(
            PropertyKey.FLOW to SettingsEventProperties.SETTINGS_PAGE,
            PropertyKey.ENTRY to UPDATE_PIN
        )
        analyticsProvider.get().trackEvents(Constants.SECURITY_PIN_CHANGED, properties)
    }

    fun trackPasswordChangeClickesEvents() {
        val properties = mutableMapOf<String, Any>()
        properties[PropertyKey.TYPE] = SECURITY
        analyticsProvider.get().trackEvents(PASSWORD_CHANGE_CLICKED, properties)
    }
}
