package tech.okcredit.home.ui.business_health_dashboard

import `in`.okcredit.analytics.AnalyticsProvider
import dagger.Lazy
import dagger.Reusable
import javax.inject.Inject

@Reusable
class BusinessHealthDashboardAnalyticsTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    object Event {
        const val BUSINESS_HEALTH_DASHBOARD_VIEW = "BH: Dashboard View"
        const val TIME_CADENCE_FILTER_CLICK = "BH: Time Cadence Filter Click"
        const val TIME_CADENCE_SELECTED = "BH: Time Cadence Selected"
        const val TREND_FEEDBACK_CLICK = "BH: Trend Feedback Click"
        const val SHOW_MORE_CLICK = "BH: Show More Click"
        const val NETWORK_ERROR = "BH: Network Error"
    }

    object BusinessHealthDashboardProperty {
        const val TIME_CADENCE = "Time Cadence"
        const val INSIGHT_ID = "Insight Id"
        const val FEEDBACK_RESPONSE = "Feedback Response"
        const val ERROR_TYPE = "Error Type"
        const val ERROR_MESSAGE = "Error Message"
        const val SOURCE = "Source"
    }

    object BusinessHealthDashboardValue {
        const val NO_INTERNET_ERROR_TYPE = "NO_INTERNET"
        const val API_ERROR_TYPE = "API"
        const val DASHBOARD_API_SOURCE = "DASHBOARD_API_ENDPOINT"
        const val FEEDBACK_API_SOURCE = "FEEDBACK_API_ENDPOINT"
    }

    fun trackDashboardIconClicked() {
        analyticsProvider.get().trackEvents(Event.BUSINESS_HEALTH_DASHBOARD_VIEW)
    }

    fun trackTimeCadenceFilterClicked() {
        analyticsProvider.get().trackEvents(Event.TIME_CADENCE_FILTER_CLICK)
    }

    fun trackTimeCadenceSelected(
        timeCadenceTitle: String,
    ) {
        val properties = mapOf(
            BusinessHealthDashboardProperty.TIME_CADENCE to timeCadenceTitle
        )
        analyticsProvider.get().trackEvents(Event.TIME_CADENCE_SELECTED, properties)
    }

    fun trackTrendFeedback(
        insightId: String,
        response: String,
        timeCadenceTitle: String,
    ) {
        val properties = mapOf(
            BusinessHealthDashboardProperty.INSIGHT_ID to insightId,
            BusinessHealthDashboardProperty.FEEDBACK_RESPONSE to response,
            BusinessHealthDashboardProperty.TIME_CADENCE to timeCadenceTitle,
        )
        analyticsProvider.get().trackEvents(Event.TREND_FEEDBACK_CLICK, properties)
    }

    fun trackShowMoreClick(
        timeCadenceTitle: String,
    ) {
        val properties = mapOf(
            BusinessHealthDashboardProperty.TIME_CADENCE to timeCadenceTitle
        )
        analyticsProvider.get().trackEvents(Event.SHOW_MORE_CLICK, properties)
    }

    fun trackNetworkError(
        source: String,
        type: String,
        errorMessage: String
    ) {
        val properties = mapOf(
            BusinessHealthDashboardProperty.SOURCE to source,
            BusinessHealthDashboardProperty.ERROR_TYPE to type,
            BusinessHealthDashboardProperty.ERROR_MESSAGE to errorMessage,
        )
        analyticsProvider.get().trackEvents(Event.NETWORK_ERROR, properties)
    }
}
