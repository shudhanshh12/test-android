package `in`.okcredit.dynamicview.events

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.dynamicview.data.model.Action
import javax.inject.Inject

class DynamicViewEventTracker @Inject constructor(private val analyticsProvider: AnalyticsProvider) {

    fun track(action: Action.Track) {
        analyticsProvider.trackEvents(action.event, action.properties)
    }
}
