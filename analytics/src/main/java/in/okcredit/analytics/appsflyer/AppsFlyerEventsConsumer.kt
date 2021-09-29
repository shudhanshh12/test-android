package `in`.okcredit.analytics.appsflyer

import `in`.okcredit.analytics.*
import android.content.Context
import com.appsflyer.AppsFlyerLib
import tech.okcredit.android.base.di.AppScope
import java.util.*
import javax.inject.Inject

@AppScope
class AppsFlyerEventsConsumer @Inject constructor(private val appsFlyer: AppsFlyerLib, val context: Context) : AnalyticEventsConsumer {

    companion object {
        val appsFlyerAnalyticsEvents = ArrayList(
            listOf(
                Event.ADD_RELATIONSHIP_STARTED,
                Event.ADD_RELATIONSHIP_SUCCESS,
                Event.ADD_TRANSACTION_STARTED,
                AnalyticsEvents.ADD_TXN_CONFIRM,
                AnalyticsEvents.VIEW_ACCOUNT_STATEMENT,
                AnalyticsEvents.IMPORT_CONTACT,
                Event.REGISTER_SUCCESSFUL,
                Event.CALL_RELATIONSHIP,
                Event.SHARE_APP,
                Event.IDENTIFIED_CATEGORY_B2B,
                Event.IDENTIFIED_CATEGORY_MOBILE,
                Event.VERIFY_MOBILE,
                Event.LOGIN_SUCCESS,
                Event.SEND_REMINDER,
                Event.VIEW_COLLECTION_MAIN,
                Event.STARTED_ADOPT_COLLECTION,
                Event.COLLECTION_ADAPTION_COMPLETED,
                Event.SEND_COLLECTION_REMINDER,
                Event.VIEW_MENU_REMINDER,
                Event.SHARED,
                Event.ONLINE_PAYMENT_CLICK,
                Event.PAY_ONLINE_PAGE_VIEW,
                Event.VIEW_QR,
            )
        )
    }

    override fun setIdentity(id: String, isSignup: Boolean) {
        appsFlyer.setCustomerUserId(id)
    }

    override fun setUserProperty(properties: Map<String, Any>) {
    }

    override fun registerSuperProperties(properties: Map<String, Any>) {
    }

    override fun setSuperProperties(properties: Map<String, Any>) {
    }

    override fun incrementTransactionCountSuperProperty() {
    }

    override fun incrementCustomerCountSuperProperty() {
    }

    override fun trackEvents(eventName: String, properties: Map<String, Any>?) {
        if (!appsFlyerAnalyticsEvents.contains(eventName)) {
            return
        }

        if (properties != null) {
            appsFlyer.trackEvent(context, eventName, properties)
        } else {
            appsFlyer.trackEvent(context, eventName, mutableMapOf())
        }
    }

    override fun flushEvents() {
    }

    override fun clearIdentity() {
    }
}
