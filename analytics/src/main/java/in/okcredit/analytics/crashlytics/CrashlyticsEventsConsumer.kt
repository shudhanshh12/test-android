package `in`.okcredit.analytics.crashlytics

import `in`.okcredit.analytics.AnalyticEventsConsumer
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.base.exceptions.ExceptionUtils
import javax.inject.Inject

@AppScope
class CrashlyticsEventsConsumer @Inject constructor() : AnalyticEventsConsumer {

    override fun setIdentity(id: String, isSignup: Boolean) {
        ExceptionUtils.setUserIdentifier(id)
    }

    override fun setUserProperty(properties: Map<String, Any>) {}

    override fun setSuperProperties(properties: Map<String, Any>) {}

    override fun registerSuperProperties(properties: Map<String, Any>) {}

    override fun trackEvents(eventName: String, properties: Map<String, Any>?) {
        ExceptionUtils.log(eventName)
    }

    override fun flushEvents() {}

    override fun incrementTransactionCountSuperProperty() {}

    override fun incrementCustomerCountSuperProperty() {}

    override fun clearIdentity() {}
}
