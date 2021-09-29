package `in`.okcredit.collection.contract

interface CollectionSyncer {

    suspend fun executeSyncCustomerCollections(businessId: String? = null)

    suspend fun executeSyncSupplierCollections(businessId: String? = null)

    suspend fun executeSyncCollectionProfile(businessId: String? = null)

    suspend fun executeSyncCollectionProfileForCustomer(customerId: String, businessId: String? = null)

    suspend fun executeSyncCollectionProfileForSupplier(accountId: String, businessId: String? = null)

    fun scheduleSyncEverything(source: String, businessId: String)

    fun scheduleSyncCollections(syncType: Int = SYNC_ALL, source: String, businessId: String)

    fun scheduleCollectionProfile(source: String, businessId: String)

    fun scheduleCollectionProfileForCustomer(customerId: String, businessId: String)

    fun scheduleCollectionProfileForSupplier(accountId: String, businessId: String)

    suspend fun scheduleSyncOnlinePayments(source: String, businessId: String? = null)

    suspend fun executeSyncKyc(merchantProfile: CollectionMerchantProfile? = null, businessId: String)

    suspend fun executeSyncOnlinePayments(businessId: String? = null)

    suspend fun syncCollectionFromNotification(collection: Collection, businessId: String)

    suspend fun syncOnlinePaymentsFromNotification(onlinePayment: CollectionOnlinePayment, businessId: String)

    companion object {
        const val COLLECTION_SYNC_TYPE = "sync_type"
        const val SYNC_ALL = 0
        const val SYNC_CUSTOMER_COLLECTIONS = 1
        const val SYNC_SUPPLIER_COLLECTIONS = 2
    }

    object Source {
        const val PERIODIC_SYNCER = "periodic_syncer"
        const val NON_ACTIVE_BUSINESSES_SYNCER = "non_active_businesses_syncer"
        const val MERCHANT_PROFILE = "merchant_profile"
        const val SYNC_SCREEN = "login_sync_screen"
        const val ADD_SUPPLIER = "add_supplier"
        const val CUSTOMER_SCREEN = "customer_screen"
        const val SUPPLIER_SCREEN = "supplier_screen"
        const val SUPPLIER_STATEMENT = "supplier_statement"
        const val GET_COLLECTION = "get_collection"
        const val PAYMENT_RESULT = "payment_result"
        const val HOME_PAYMENTS = "home_payments"
        const val MERCHANT_QR = "merchant_qr"
        const val ONLINE_PAYMENTS = "online_payments"
        const val APP_LAUNCH = "app_launch"
    }
}
