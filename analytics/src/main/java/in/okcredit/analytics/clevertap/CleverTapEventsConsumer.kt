package `in`.okcredit.analytics.clevertap

import `in`.okcredit.analytics.AnalyticEventsConsumer
import `in`.okcredit.analytics.IdentityProperties
import com.clevertap.android.sdk.CleverTapAPI
import dagger.Lazy
import tech.okcredit.android.base.di.AppScope
import javax.inject.Inject

@AppScope
class CleverTapEventsConsumer @Inject constructor(private val clevertap: Lazy<CleverTapAPI?>) : AnalyticEventsConsumer {

    override fun setIdentity(id: String, isSignup: Boolean) {
        val properties = mutableMapOf<String, Any>()
        properties[IdentityProperties.IDENTITY] = id
        clevertap.get()?.onUserLogin(properties)
        clevertap.get()?.enableDeviceNetworkInfoReporting(true)
    }

    override fun setUserProperty(properties: Map<String, Any>) {
        clevertap.get()?.pushProfile(properties)
    }

    override fun setSuperProperties(properties: Map<String, Any>) {
        // no super properties for clevertap
    }

    override fun registerSuperProperties(properties: Map<String, Any>) {
    }

    override fun incrementTransactionCountSuperProperty() {
        // no super properties for clevertap
    }

    override fun incrementCustomerCountSuperProperty() {
        // no super properties for clevertap
    }

    override fun trackEvents(eventName: String, properties: Map<String, Any>?) {
        clevertap.get()?.pushEvent(eventName, properties)
    }

    override fun flushEvents() {}

    override fun clearIdentity() {
    }
}
