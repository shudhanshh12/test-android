package tech.okcredit.home.ui.acccountV2.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import javax.inject.Inject

class AccountEventTacker @Inject constructor(
    private val analyticsProvider: Lazy<AnalyticsProvider>
) {
    fun trackAccountEvents(event: String, screen: String) {
        val properties = mapOf(PropertyKey.SCREEN to screen)
        analyticsProvider.get().trackEvents(event, properties)
    }
}
