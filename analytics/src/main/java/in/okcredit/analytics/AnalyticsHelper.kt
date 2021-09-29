package `in`.okcredit.analytics

/**
 * Created by harsh on 10/01/18.
 */
interface AnalyticsHelper {

    fun setUserProperty(key: String, value: String?)

    fun track(eventName: String, props: EventProperties?)
}
