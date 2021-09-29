package `in`.okcredit.analytics.mixpanel

import `in`.okcredit.analytics.*
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Lazy
import org.json.JSONException
import org.json.JSONObject
import tech.okcredit.android.base.di.AppScope
import timber.log.Timber
import javax.inject.Inject

@AppScope
class MixpanelEventsConsumer @Inject constructor(private val mixpanel: Lazy<MixpanelAPI>) : AnalyticEventsConsumer {

    override fun setIdentity(id: String, isSignup: Boolean) {
        if (isSignup) {
            mixpanel.get().alias(id, mixpanel.get().distinctId)
        }
        mixpanel.get().identify(id)
        mixpanel.get().people.identify(id)
    }

    override fun setUserProperty(properties: Map<String, Any>) {
        for ((key, value) in properties) {
            mixpanel.get().people.set(handleStandardProps(key), value)
        }
    }

    override fun registerSuperProperties(properties: Map<String, Any>) {
        try {
            val props = JSONObject()
            for ((key, value) in properties) {
                props.put(key, value)
            }
            mixpanel.get().registerSuperProperties(props)
        } catch (e: JSONException) {
            Timber.e("Failed to send global event properties for mixpanel.get()")
        }
    }

    override fun setSuperProperties(properties: Map<String, Any>) {
        try {
            val props = JSONObject()
            for ((key, value) in properties) {
                props.put(key, value)
            }
            mixpanel.get().registerSuperProperties(props)
        } catch (e: JSONException) {
            Timber.e("Failed to send global event properties for mixpanel.get()")
        }
    }

    override fun incrementTransactionCountSuperProperty() {
        val prop = mixpanel.get().superProperties

        if (prop.has(SuperProperties.CUSTOMER_TRANSACTION_COUNT)) {
            try {
                var transactionCount = prop.getInt(SuperProperties.CUSTOMER_TRANSACTION_COUNT)
                transactionCount ++
                prop.put(SuperProperties.CUSTOMER_TRANSACTION_COUNT, transactionCount)
                mixpanel.get().registerSuperProperties(prop)
            } catch (e: Exception) {
                Timber.e("Analytics Failed incrementing super properties")
            }
        }
    }

    override fun incrementCustomerCountSuperProperty() {
        val prop = mixpanel.get().superProperties

        if (prop.has(SuperProperties.CUSTOMER_COUNT)) {
            try {
                var customerCount = prop.getInt(SuperProperties.CUSTOMER_COUNT)
                customerCount ++
                prop.put(SuperProperties.CUSTOMER_COUNT, customerCount)
                mixpanel.get().registerSuperProperties(prop)
            } catch (e: Exception) {
                Timber.e("Analytics Failed incrementing super properties")
            }
        }
    }

    override fun trackEvents(eventName: String, properties: Map<String, Any>?) {

        if (properties == null) {
            mixpanel.get().track(eventName)
        } else {
            val props = JSONObject()
            for ((key, value) in properties) {
                props.put(key, value)
            }
            mixpanel.get().track(eventName, props)
        }
    }

    override fun flushEvents() {
        mixpanel.get().flush()
    }

    private fun handleStandardProps(key: String): String {
        return when (key) {
            "name" -> "\$name"
            "phone" -> "\$phone"
            else -> key
        }
    }

    override fun clearIdentity() {
    }
}
