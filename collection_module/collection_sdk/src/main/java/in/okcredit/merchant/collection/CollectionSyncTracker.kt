package `in`.okcredit.merchant.collection

import `in`.okcredit.analytics.IAnalyticsProvider
import dagger.Lazy
import javax.inject.Inject

class CollectionSyncTracker @Inject constructor(private val analyticsProvider: Lazy<IAnalyticsProvider>) {

    object Events {
        const val SCHEDULE_SYNC_COLLECTION_EVERYTHING = "schedule_sync_collections"
        const val SCHEDULE_SYNC_COLLECTIONS = "schedule_sync_collections"
        const val SCHEDULE_SYNC_MERCHANT_PAYMENT = "schedule_sync_merchant_payment"
        const val SCHEDULE_SYNC_MERCHANT_COLLECTION_PROFILE = "schedule_sync_merchant_profile"
        const val SCHEDULE_SYNC_CUSTOMER_COLLECTION_PROFILE = "schedule_sync_customer_profile"
        const val SCHEDULE_SYNC_SUPPLIER_COLLECTION_PROFILE = "schedule_sync_supplier_profile"

        const val EXECUTE_SYNC_ONLINE_PAYMENTS = "online_payments_execute_sync"
        const val EXECUTE_SYNC_CUSTOMER_COLLECTIONS = "customer_collections_execute_sync"
        const val EXECUTE_SYNC_SUPPLIER_COLLECTIONS = "supplier_collections_execute_sync"

        const val EXECUTE_SYNC_MERCHANT_PROFILE = "merchant_profile_execute_sync"
        const val EXECUTE_SYNC_CUSTOMER_PROFILE = "customer_profile_execute_sync"
        const val EXECUTE_SYNC_SUPPLIER_PROFILE = "supplier_profile_execute_sync"

        const val ONLINE_PAYMENT_SYNC_ERROR = "online_payment_sync_error"
        const val ONLINE_PAYMENT_SYNC_SUCCESS = "online_payment_sync_success"
        const val ONLINE_PAYMENT_SYNC_NOTIFICATION_RECEIVED = "online_payment_sync_notification_received"

        const val COLLECTION_SYNC_NOTIFICATION_RECEIVED = "collection_sync_notification_received"
        const val CUSTOMER_COLLECTIONS_SYNC_ERROR = "customer_collections_sync_error"
        const val CUSTOMER_COLLECTIONS_SYNC_SUCCESS = "customer_collections_sync_success"

        const val SUPPLIER_COLLECTIONS_SYNC_ERROR = "supplier_collections_sync_error"
        const val SUPPLIER_COLLECTIONS_SYNC_SUCCESS = "supplier_collections_sync_success"

        const val MERCHANT_PROFILE_SYNC_ERROR = "merchant_profile_sync_error"
        const val MERCHANT_PROFILE_SYNC_SUCCESS = "merchant_profile_sync_success"

        const val CUSTOMER_PROFILE_SYNC_ERROR = "customer_profile_sync_error"
        const val CUSTOMER_PROFILE_SYNC_SUCCESS = "customer_profile_sync_success"

        const val SUPPLIER_PROFILE_SYNC_ERROR = "supplier_profile_sync_error"
        const val SUPPLIER_PROFILE_SYNC_SUCCESS = "supplier_profile_sync_success"
    }

    object PropertyKey {
        const val SOURCE = "source"
        const val CUSTOMER_ID = "customer_id"
        const val SUPPLIER_ID = "supplier_id"
        const val ERROR = "error"
        const val MESSAGE = "message"
        const val PROFILE_LIST_COUNT = "list_count"
        const val DESTINATION_PRESENT = "destination_present"
        const val QR_CODE_PRESENT = "qr_intent_present"
        const val LAST_SYNC = "last_sync"
        const val SYNC_TYPE = "syncType"
        const val AMOUNT = "amount"
        const val COLLECTION_ID = "collection_id"
        const val STATUS = "status"
        const val TYPE = "type"
    }

    fun trackScheduleSyncEverything(source: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SCHEDULE_SYNC_COLLECTION_EVERYTHING,
            properties = mapOf(PropertyKey.SOURCE to source),
        )
    }

    fun trackScheduleSyncCollections(source: String, syncType: Int) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SCHEDULE_SYNC_COLLECTIONS,
            properties = mapOf(
                PropertyKey.SOURCE to source,
                PropertyKey.SYNC_TYPE to syncType
            )
        )
    }

    fun trackScheduleSyncMerchantPayment(source: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SCHEDULE_SYNC_MERCHANT_PAYMENT,
            properties = mapOf(PropertyKey.SOURCE to source)
        )
    }

    fun trackScheduleSyncMerchantProfile(source: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SCHEDULE_SYNC_MERCHANT_COLLECTION_PROFILE,
            properties = mapOf(PropertyKey.SOURCE to source)
        )
    }

    fun trackScheduleSyncCustomerProfile(customerId: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SCHEDULE_SYNC_CUSTOMER_COLLECTION_PROFILE,
            properties = mapOf(PropertyKey.CUSTOMER_ID to customerId)
        )
    }

    fun trackScheduleSyncSupplierProfile(supplierId: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SCHEDULE_SYNC_SUPPLIER_COLLECTION_PROFILE,
            properties = mapOf(PropertyKey.SUPPLIER_ID to supplierId)
        )
    }

    fun trackExecuteSyncOnlinePayments() {
        analyticsProvider.get().trackEvents(
            eventName = Events.EXECUTE_SYNC_ONLINE_PAYMENTS,
        )
    }

    fun trackExecuteSyncCustomerCollections(lastSyncTime: Long) {
        analyticsProvider.get().trackEvents(
            eventName = Events.EXECUTE_SYNC_CUSTOMER_COLLECTIONS,
            properties = mapOf(
                PropertyKey.LAST_SYNC to lastSyncTime,
            )
        )
    }

    fun trackExecuteSyncSupplierCollections(lastSyncTime: Long) {
        analyticsProvider.get().trackEvents(
            eventName = Events.EXECUTE_SYNC_SUPPLIER_COLLECTIONS,
            properties = mapOf(PropertyKey.LAST_SYNC to lastSyncTime)
        )
    }

    fun trackExecuteSyncMerchantProfile() {
        analyticsProvider.get().trackEvents(
            eventName = Events.EXECUTE_SYNC_MERCHANT_PROFILE,
        )
    }

    fun trackExecuteSyncCustomerProfile(customerId: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.EXECUTE_SYNC_CUSTOMER_PROFILE,
            properties = mapOf(PropertyKey.CUSTOMER_ID to customerId)
        )
    }

    fun trackExecuteSyncSupplierProfile(supplierId: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.EXECUTE_SYNC_SUPPLIER_PROFILE,
            properties = mapOf(PropertyKey.SUPPLIER_ID to supplierId)
        )
    }

    fun trackOnlinePaymentsSyncSuccess(size: Int, ids: String, status: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.ONLINE_PAYMENT_SYNC_SUCCESS,
            properties = mapOf(
                PropertyKey.PROFILE_LIST_COUNT to size,
                PropertyKey.COLLECTION_ID to ids,
                PropertyKey.STATUS to status
            )
        )
    }

    fun trackOnlinePaymentsSyncError(name: String, message: String?) {
        analyticsProvider.get().trackEvents(
            eventName = Events.ONLINE_PAYMENT_SYNC_ERROR,
            properties = mapOf(
                PropertyKey.ERROR to name,
                PropertyKey.MESSAGE to (message ?: "")
            )
        )
    }

    fun trackCustomerCollectionsSyncSuccess(size: Int, ids: String, status: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.CUSTOMER_COLLECTIONS_SYNC_SUCCESS,
            properties = mapOf(
                PropertyKey.PROFILE_LIST_COUNT to size,
                PropertyKey.COLLECTION_ID to ids,
                PropertyKey.STATUS to status
            )
        )
    }

    fun trackCustomerCollectionsSyncError(name: String, message: String?) {
        analyticsProvider.get().trackEvents(
            eventName = Events.CUSTOMER_COLLECTIONS_SYNC_ERROR,
            properties = mapOf(
                PropertyKey.ERROR to name,
                PropertyKey.MESSAGE to (message ?: "")
            )
        )
    }

    fun trackSupplierCollectionsSyncSuccess(size: Int, ids: String, status: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SUPPLIER_COLLECTIONS_SYNC_SUCCESS,
            properties = mapOf(
                PropertyKey.PROFILE_LIST_COUNT to size,
                PropertyKey.COLLECTION_ID to ids,
                PropertyKey.STATUS to status
            )
        )
    }

    fun trackSupplierCollectionsSyncError(name: String, message: String?) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SUPPLIER_COLLECTIONS_SYNC_ERROR,
            properties = mapOf(
                PropertyKey.ERROR to name,
                PropertyKey.MESSAGE to (message ?: "")
            )
        )
    }

    fun trackMerchantProfileSyncSuccess(merchantVpaPresent: Boolean, paymentAddressPresent: Boolean, size: Int) {
        analyticsProvider.get().trackEvents(
            eventName = Events.MERCHANT_PROFILE_SYNC_SUCCESS,
            properties = mapOf(
                PropertyKey.QR_CODE_PRESENT to merchantVpaPresent,
                PropertyKey.DESTINATION_PRESENT to paymentAddressPresent,
                PropertyKey.PROFILE_LIST_COUNT to size
            )
        )
    }

    fun trackMerchantProfileSyncError(name: String, message: String?) {
        analyticsProvider.get().trackEvents(
            eventName = Events.MERCHANT_PROFILE_SYNC_ERROR,
            properties = mapOf(
                PropertyKey.ERROR to name,
                PropertyKey.MESSAGE to (message ?: "")
            )
        )
    }

    fun trackCustomerProfileSyncSuccess(qrIntentPresent: Boolean, customerId: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.CUSTOMER_PROFILE_SYNC_SUCCESS,
            properties = mapOf(
                PropertyKey.CUSTOMER_ID to customerId,
                PropertyKey.QR_CODE_PRESENT to qrIntentPresent,
            )
        )
    }

    fun trackCustomerProfileSyncError(name: String, message: String?, customerId: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.CUSTOMER_PROFILE_SYNC_ERROR,
            properties = mapOf(
                PropertyKey.CUSTOMER_ID to customerId,
                PropertyKey.ERROR to name,
                PropertyKey.MESSAGE to (message ?: "")
            )
        )
    }

    fun trackSupplierProfileSyncSuccess(paymentAddressPresent: Boolean, accountId: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SUPPLIER_PROFILE_SYNC_SUCCESS,
            properties = mapOf(
                PropertyKey.SUPPLIER_ID to accountId,
                PropertyKey.DESTINATION_PRESENT to paymentAddressPresent,
            )
        )
    }

    fun trackSupplierProfileSyncError(name: String, message: String?, accountId: String) {
        analyticsProvider.get().trackEvents(
            eventName = Events.SUPPLIER_PROFILE_SYNC_ERROR,
            properties = mapOf(
                PropertyKey.SUPPLIER_ID to accountId,
                PropertyKey.ERROR to name,
                PropertyKey.MESSAGE to (message ?: "")
            )
        )
    }

    fun trackMerchantPaymentSyncNotificationReceived(
        id: String,
        amount: Double,
        status: Int,
        type: String,
        error: String,
    ) {
        analyticsProvider.get().trackEvents(
            eventName = Events.ONLINE_PAYMENT_SYNC_NOTIFICATION_RECEIVED,
            properties = mapOf(
                PropertyKey.COLLECTION_ID to id,
                PropertyKey.AMOUNT to amount,
                PropertyKey.TYPE to type,
                PropertyKey.STATUS to status,
                PropertyKey.ERROR to error
            )
        )
    }

    fun trackCollectionSyncNotificationReceived(
        id: String,
        customerId: String,
        error: String,
        status: Int,
    ) {
        analyticsProvider.get().trackEvents(
            eventName = Events.COLLECTION_SYNC_NOTIFICATION_RECEIVED,
            properties = mapOf(
                PropertyKey.COLLECTION_ID to id,
                PropertyKey.ERROR to error,
                PropertyKey.CUSTOMER_ID to customerId,
                PropertyKey.STATUS to status
            )
        )
    }
}
