package `in`.okcredit.analytics.firebase

import `in`.okcredit.analytics.AnalyticEventsConsumer
import `in`.okcredit.analytics.IdentityProperties
import `in`.okcredit.analytics.UserProperties
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Lazy
import tech.okcredit.android.base.di.AppScope
import javax.inject.Inject

@AppScope
class FirebaseEventsConsumer @Inject constructor(private val firebase: Lazy<FirebaseAnalytics>) : AnalyticEventsConsumer {
    override fun setIdentity(id: String, isSignup: Boolean) {
        firebase.get().setUserId(id)
        firebase.get().setUserProperty(IdentityProperties.MERCHANT_ID, id)
    }

    override fun setUserProperty(properties: Map<String, Any>) {
        val prop = properties.filterKeys { it != UserProperties.LANGUAGE_DEVICE }
        for ((key, value) in prop) {
            var newKey = key.replace(" ", "_")
            newKey = newKey.replace("[(-+.^:,)]".toRegex(), "_")
            firebase.get().setUserProperty(newKey.toLowerCase(), value.toString())
        }
    }

    override fun registerSuperProperties(properties: Map<String, Any>) {
    }

    override fun setSuperProperties(properties: Map<String, Any>) {
        // no super properties for firebase
    }

    override fun incrementTransactionCountSuperProperty() {
        // no super properties for firebase
    }

    override fun incrementCustomerCountSuperProperty() {
        // no super properties for firebase
    }

    override fun trackEvents(eventName: String, properties: Map<String, Any>?) {
        var newEventName = eventName.replace(" ", "_")
        newEventName = newEventName.replace("[(-+.^:,)]".toRegex(), "")

        if (properties == null) {
            val params = Bundle()
            firebase.get().logEvent(newEventName, params)
        } else {
            val bundle = Bundle()
            for ((key, value) in properties) {
                var newKey = key.replace(" ", "_")
                newKey = newKey.replace("[(-+.^:,)]".toRegex(), "_")
                bundle.putString(newKey, value.toString())
            }
            firebase.get().logEvent(newEventName, bundle)
        }
    }

    override fun flushEvents() {}

    override fun clearIdentity() {
    }
}
