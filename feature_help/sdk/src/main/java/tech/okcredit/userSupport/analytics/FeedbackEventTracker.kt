package tech.okcredit.userSupport.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey
import dagger.Lazy
import javax.inject.Inject

class FeedbackEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        const val FEEDBACK_EVENT = "View Feedback"
        const val FEEDBACK_INTERACTION_BACK = "Back"
        const val FEEDBACK_INTERACTION_SUBMIT = "Submited"
    }

    fun trackViewChat(source: String, interaction: String) {
        val properties = HashMap<String, String>().apply {
            this[PropertyKey.SOURCE] = source
            this[PropertyKey.INTERACTION] = interaction
        }
        analyticsProvider.get().trackEvents(FEEDBACK_EVENT, properties)
    }
}
