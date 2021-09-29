package tech.okcredit.web

import `in`.okcredit.analytics.AnalyticsProvider
import timber.log.Timber
import javax.inject.Inject

class WebTracker @Inject constructor(private val analyticsProvider: AnalyticsProvider) {

    object Experiments {
        const val UNKNOWN = "unknown"
    }

    object Event {
        const val WEBVIEW_BOOT_TIME = "Webview: Boot Time"
        const val WEBVIEW_PAGE_LOAD_TIME = "Webview: Page Load Time"
    }

    fun trackBootUpTime(time: Long, experiment: String, campaignId: String?, url: String?) {
        val properties = mutableMapOf<String, Any>().apply {
            this["EXPERIMENT"] = experiment
            this["CAMPAIGN_ID"] = campaignId ?: ""
            this["LOAD_TIME"] = time
            this["URL"] = url ?: ""
        }
        Timber.d("props: %s", properties)
        analyticsProvider.trackEvents(Event.WEBVIEW_BOOT_TIME, properties)
    }

    fun trackPageLoadTime(time: Long, experiment: String, campaignId: String?, url: String?) {
        val properties = mutableMapOf<String, Any>().apply {
            this["EXPERIMENT"] = experiment
            this["CAMPAIGN_ID"] = campaignId ?: ""
            this["LOAD_TIME"] = time
            this["URL"] = url ?: ""
        }
        Timber.d("props: %s", properties)
        analyticsProvider.trackEvents(Event.WEBVIEW_PAGE_LOAD_TIME, properties)
    }
}
