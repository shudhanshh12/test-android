package tech.okcredit.applock.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import javax.inject.Inject

class AppLockEventTracker @Inject constructor(
    val analyticsProvider: Lazy<AnalyticsProvider>,
) {
    fun trackEvents(
        eventName: String,
        screen: String? = null,
        flow: String? = null,
        entry: String? = null,
    ) {
        val properties = mutableMapOf<String, Any>()
        if (screen != null) properties[PropertyKey.SCREEN] = screen
        if (flow != null) properties[PropertyKey.FLOW] = flow
        if (entry != null) properties[PropertyKey.ENTRY] = entry
        analyticsProvider.get().trackEvents(eventName, properties)
    }
}
