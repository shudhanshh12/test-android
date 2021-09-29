package `in`.okcredit.frontend.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey.ACCOUNT_ID
import `in`.okcredit.analytics.PropertyKey.FOCAL_AREA
import `in`.okcredit.analytics.PropertyKey.RELATION
import `in`.okcredit.analytics.PropertyKey.SCREEN
import `in`.okcredit.analytics.PropertyKey.SOURCE
import `in`.okcredit.analytics.PropertyKey.TYPE
import `in`.okcredit.analytics.PropertyKey.VALUE
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.SuperProperties.MERCHANT_ID
import dagger.Lazy
import javax.inject.Inject

class SupplierEventTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        // Events
        const val IN_APP_NOTI_DISPLAYED = "InAppNotification Displayed"
        const val IN_APP_NOTI_CLICKED = "InAppNotification Clicked"
        const val CHOOSE_PAYMENT_OPTION = "Choose payment option"

        // Property Keys
        const val KEY_EASY_PAY = "easy_pay"

        // Property Values
        const val FIRST_SUPPLIER_CUSTOMER = "FirstSupplier Customer"
        const val PAY_ONLINE_REMINDER_EDUCATION = "PAY ONLINE REMINDER EDUCATION"
    }

    fun trackPaymentOption(accountId: String, merchantId: String, easyPay: Boolean) {
        val properties = HashMap<String, Any>().apply {
            this[SCREEN] = PropertyValue.SUPPLIER_SCREEN
            this[RELATION] = PropertyValue.SUPPLIER
            this[MERCHANT_ID] = merchantId
            this[KEY_EASY_PAY] = easyPay
            this[ACCOUNT_ID] = accountId
        }
        analyticsProvider.get().trackEvents(CHOOSE_PAYMENT_OPTION, properties)
    }

    fun trackEvents(
        eventName: String,
        type: String? = null,
        screen: String? = null,
        relation: String? = null,
        source: String? = null,
        value: Boolean? = null,
        focalArea: Boolean? = null,
    ) {
        val properties = HashMap<String, Any>().apply {
            screen?.let {
                this[SCREEN] = screen
            }

            type?.let {
                this[TYPE] = type
            }

            relation?.let {
                this[RELATION] = relation
            }

            value?.let {
                this[VALUE] = value
            }

            source?.let {
                this[SOURCE] = source
            }

            focalArea?.let {
                this[FOCAL_AREA] = focalArea
            }
        }

        analyticsProvider.get().trackEvents(eventName, properties)
    }

    fun trackInAppNotificationDisplayed(screen: String? = null, type: String? = null) {
        trackEvents(IN_APP_NOTI_DISPLAYED, screen = screen, type = type)
    }

    fun trackInAppNotificationClicked(screen: String? = null, type: String? = null, focalArea: Boolean? = null) {
        trackEvents(IN_APP_NOTI_CLICKED, screen = screen, type = type, focalArea = focalArea)
    }
}
