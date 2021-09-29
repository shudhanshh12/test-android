package `in`.okcredit.backend.analytics.crashlytics

import `in`.okcredit.analytics.AnalyticsHelper
import `in`.okcredit.analytics.EventProperties
import tech.okcredit.base.exceptions.ExceptionUtils.Companion.log

/**
 * Created by harsh on 10/01/18.
 */
class CrashlyticsAnalyticsHelper : AnalyticsHelper {
    override fun setUserProperty(key: String, value: String?) {}

    override fun track(eventName: String, props: EventProperties?) {
        log(eventName)
    }
}
