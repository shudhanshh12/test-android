package `in`.okcredit.analytics.logger

import `in`.okcredit.analytics.AnalyticEventsConsumer
import `in`.okcredit.analytics.helpers.AnalyticsNotificationHelper
import dagger.Lazy
import tech.okcredit.android.base.di.AppScope
import timber.log.Timber
import javax.inject.Inject

@AppScope
class DebugLogger @Inject constructor(private val analyticsNotificationHelper: Lazy<AnalyticsNotificationHelper>) : AnalyticEventsConsumer {

    override fun setIdentity(id: String, isSignup: Boolean) {
        analyticsNotificationHelper.get().addInNotification("setIdentity", id)
        Timber.i("[Analytics-setIdentity]: $id")
    }

    override fun setUserProperty(properties: Map<String, Any>) {
        analyticsNotificationHelper.get().addInNotification("setUserProperty", properties.toString())
        Timber.i("[Analytics-setUserProperty]: $properties")
    }

    override fun registerSuperProperties(properties: Map<String, Any>) {
        Timber.i("[Analytics-registerSuperProperty]: $properties")
    }

    override fun setSuperProperties(properties: Map<String, Any>) {
        analyticsNotificationHelper.get().addInNotification("setSuperProperties", properties.toString())
        Timber.i("[Analytics-setSuperProperties]: $properties")
    }

    override fun incrementTransactionCountSuperProperty() {
        analyticsNotificationHelper.get().addInNotification("incrementTransactionCountSuperProperty", "")
        Timber.i("[Analytics-incrementTransactionCountSuperProperty]")
    }

    override fun incrementCustomerCountSuperProperty() {
        analyticsNotificationHelper.get().addInNotification("incrementCustomerCountSuperProperty", "")
        Timber.i("[Analytics-incrementCustomerCountSuperProperty]")
    }

    override fun trackEvents(eventName: String, properties: Map<String, Any>?) {
        analyticsNotificationHelper.get().addInNotification(eventName, properties.toString())
        if (properties != null) {
            Timber.i("[Analytics] $eventName: $properties")
        } else {
            Timber.i("[Analytics] $eventName")
        }
    }

    override fun flushEvents() {
        Timber.i("[Analytics] flushing events")
    }

    override fun clearIdentity() { }
}
