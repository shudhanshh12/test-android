package `in`.okcredit.collection_ui.analytics

import `in`.okcredit.analytics.AnalyticsProvider
import `in`.okcredit.analytics.PropertyKey.SOURCE
import dagger.Lazy
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import javax.inject.Inject

class OnlineCollectionTracker @Inject constructor(private val analyticsProvider: Lazy<AnalyticsProvider>) {

    companion object {
        private const val SCREEN = "Screen"
        const val QR_SAVE_SHARE_TYPE = "qr_save_share"
        const val QR_ONLINE_COLLECTION_TYPE = "tracking_payments"
        const val QR_MENU_TYPE = "payments_more_option"
    }

    object Screen {
        const val collectionQr = "Collection QR"
        const val collectionPaymentsView = "Collection Payments View"
        const val collectionPaymentTransaction = "Collection Payment transaction"
    }

    object Events {
        const val QR_FIRST_VIEW = "qr_first_View"
        const val QR_FIRST_LOAD = "qr_first_load"
        const val QR_FIRST_DATE_RANGE_SELECT = "qr_first_date_range_select"
        const val QR_FIRST_DATE_RANGE_UPDATE = "qr_first_date_range_update"
        const val QR_FIRST_TRANSACTION_VIEW = "qr_first_transaction_view"
        const val QR_FIRST_RELATIONSHIP_LINK_START = "qr_first_relationship_link_start"
        const val QR_FIRST_RELATIONSHIP_CANCEL = "qr_first_relationship_cancel"
        const val QR_FIRST_RELATIONSHIP_CONFIRM = "qr_first_relationship_confirm"
        const val QR_FIRST_RELATIONSHIP_LINK_COMPLETE = "qr_first_relationship_link_complete"
        const val IN_APP_NOTIFICATION_DISPLAYED = "InAppNotification Displayed"
        const val IN_APP_NOTIFICATION_CLICKED = "InAppNotification Clicked"
        const val REFUND_TO_CUSTOMER_STARTED = "Refund to Customer Started"
        const val REFUND_TO_CUSTOMER_POP_UP_OPENED = "Refund to Customer PopUp Opened"
        const val REFUND_TO_CUSTOMER_CONFIRMED = "Refund to Customer Confirmed"
        const val REFUND_TO_CUSTOMER_CANCELLED = "Refund to Customer Cancelled"
        const val CHAT_WITH_SUPPORT = "Chat With Support"
    }

    fun trackClickEventOnlineCollection() {
        val properties = HashMap<String, String>().apply {
            this[SCREEN] = Screen.collectionQr
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_VIEW, properties)
    }

    fun trackLoadEventOnlineCollection(source: String) {
        val properties = HashMap<String, String>().apply {
            this[SOURCE] = source
            this[SCREEN] = Screen.collectionQr
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_LOAD, properties)
    }

    fun trackDateRangeSelect(source: String) {
        val properties = HashMap<String, String>().apply {
            this[SOURCE] = source
            this[SCREEN] = Screen.collectionQr
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_DATE_RANGE_SELECT, properties)
    }

    fun trackDateRangeUpdate(value: String, startDate: DateTime, endDateTime: DateTime, source: String) {
        val properties = HashMap<String, String>().apply {
            this[SOURCE] = source
            this[SCREEN] = Screen.collectionQr
            this["value"] = value
            this["date_range"] = DateTimeUtils.formatDateOnly(startDate) + DateTimeUtils.formatDateOnly(endDateTime)
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_DATE_RANGE_UPDATE, properties)
    }

    fun trackClickTransactionDetail(collectionId: String, source: String) {
        val properties = HashMap<String, String>().apply {
            this[SOURCE] = source
            this[SCREEN] = Screen.collectionPaymentsView
            this["collection_id"] = collectionId
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_TRANSACTION_VIEW, properties)
    }

    fun trackClickAddToKhata(screen: String, collectionId: String, source: String) {
        val properties = HashMap<String, String>().apply {
            this[SOURCE] = source
            this[SCREEN] = screen
            this["collection_id"] = collectionId
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_RELATIONSHIP_LINK_START, properties)
    }

    fun trackClickEventCancelTagging(collectionId: String, source: String) {
        val properties = HashMap<String, String>().apply {
            this[SOURCE] = source
            this["collection_id"] = collectionId
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_RELATIONSHIP_CANCEL, properties)
    }

    fun trackClickEventConfirmTagging(
        collectionId: String?,
        customerId: String?,
        mobile: String? = null,
        source: String
    ) {
        val properties = HashMap<String, String>().apply {
            this["collection_id"] = collectionId ?: ""
            this["account_id"] = customerId ?: ""
            this["mobile"] = mobile ?: ""
            this[SOURCE] = source
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_RELATIONSHIP_CONFIRM, properties)
    }

    fun trackOnSuccessfullTagging(
        collectionId: String?,
        customerId: String?,
        mobile: String?,
        collectionDate: DateTime?,
        source: String,
    ) {
        val properties = HashMap<String, String>().apply {
            this["collection_id"] = collectionId ?: ""
            this["account_id"] = customerId ?: ""
            this["mobile"] = mobile ?: ""
            this["collection_date"] = DateTimeUtils.formatDateOnly(collectionDate)
            this[SOURCE] = source
        }
        analyticsProvider.get().trackEvents(Events.QR_FIRST_RELATIONSHIP_LINK_COMPLETE, properties)
    }

    fun trackQrScreenEducationDisplayed(type: String, variant: String) {
        val properties = HashMap<String, Any>().apply {
            this["type"] = type
            this["variant"] = variant
            this["screen"] = Screen.collectionQr
        }
        analyticsProvider.get().trackEvents(Events.IN_APP_NOTIFICATION_DISPLAYED, properties)
    }

    fun trackQrScreenEducationClicked(type: String, variant: String, focal: Boolean) {
        val properties = HashMap<String, Any>().apply {
            this["type"] = type
            this["variant"] = variant
            this["screen"] = Screen.collectionQr
            this["focal"] = focal
        }
        analyticsProvider.get().trackEvents(Events.IN_APP_NOTIFICATION_CLICKED, properties)
    }

    fun trackRefundToCustomerClicked(txnId: String, source: String) {
        val properties = HashMap<String, Any>().apply {
            this["Transaction ID"] = txnId
            this[SOURCE] = source
        }
        analyticsProvider.get().trackEvents(Events.REFUND_TO_CUSTOMER_STARTED, properties)
    }

    fun trackRefundToCustomerPopUpOpened(txnId: String) {
        val properties = HashMap<String, Any>().apply {
            this["Transaction ID"] = txnId
        }
        analyticsProvider.get().trackEvents(Events.REFUND_TO_CUSTOMER_POP_UP_OPENED, properties)
    }

    fun trackClickedOnRefundOnRefundDialog(txnId: String) {
        val properties = HashMap<String, Any>().apply {
            this["Transaction ID"] = txnId
        }
        analyticsProvider.get().trackEvents(Events.REFUND_TO_CUSTOMER_CONFIRMED, properties)
    }

    fun trackRefundToCustomerCancelled(txnId: String) {
        val properties = HashMap<String, Any>().apply {
            this["Transaction ID"] = txnId
        }
        analyticsProvider.get().trackEvents(Events.REFUND_TO_CUSTOMER_CANCELLED, properties)
    }

    fun trackChatWithSupport(txnId: String, screen: String, source: String) {
        val properties = HashMap<String, Any>().apply {
            this["Transaction ID"] = txnId
            this["Screen"] = screen
            this[SOURCE] = source
        }
        analyticsProvider.get().trackEvents(Events.CHAT_WITH_SUPPORT, properties)
    }
}
