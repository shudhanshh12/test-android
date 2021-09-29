package `in`.okcredit.collection.contract

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime

interface CollectionRepository {
    // get list of collections
    fun listCollections(businessId: String): Observable<List<Collection>>

    // get list of collections of a customer or supplier
    fun getCollectionsOfCustomerOrSupplier(customerId: String, businessId: String): Observable<List<Collection>>

    // get details of a collection
    fun getCollection(collection_id: String, businessId: String): Observable<Collection>

    // get collection related info of merchant
    fun getCollectionMerchantProfile(businessId: String): Observable<CollectionMerchantProfile>

    // get collection related info of customer
    fun getCollectionCustomerProfile(customer_id: String, businessId: String): Observable<CollectionCustomerProfile>

    fun listCollectionCustomerProfiles(businessId: String): Observable<List<CollectionCustomerProfile>>

    // return a list of customer id with qr_intent pair
    fun listCustomerQrIntents(businessId: String): Observable<Map<String, String?>>

    // Set collection destination
    fun setActiveDestination(
        collectionMerchantProfile: CollectionMerchantProfile,
        async: Boolean = false,
        referralMerchant: String = "",
        businessId: String,
    ): Single<ApiMessages.MerchantCollectionProfileResponse>

    // verify payment address
    fun validatePaymentAddress(
        payment_address_type: String,
        payment_address: String,
        businessId: String,
    ): Single<Pair<Boolean, String>>

    // clear all data from collection DB. using when user logout
    fun clearLocalData(): Completable

    // get link of all collection share infos
    fun listCollectionShareInfos(businessId: String): Observable<List<CollectionShareInfo>>

    // add a share info
    fun insertCollectionShareInfo(req: CollectionShareInfo, businessId: String): Completable

    // delete collection share info of a customer
    fun deleteCollectionShareInfoOfCustomer(customerId: String): Completable

    // is User Set Destination
    fun isCollectionActivated(): Observable<Boolean>

    // create bulk collections
    fun createBatchCollection(
        customerIds: List<String>,
        businessId: String,
    ): Completable

    // get collection related info of merchant
    fun getPredictedCollectionMerchantProfile(businessId: String): Single<CollectionMerchantProfile>

    // get collection related info of supplier
    fun getSupplierCollectionProfile(accountId: String, businessId: String): Observable<CollectionCustomerProfile>

    // get payment destination related info of supplier
    fun getSupplierPaymentDestination(accountId: String, businessId: String): Single<CollectionCustomerProfile>

    fun enableCustomerPayment(businessId: String): Completable

    fun getKycStatus(businessId: String): Observable<String>

    fun getKycExternalInfo(businessId: String): Observable<KycExternalInfo?>

    fun listOnlinePayments(businessId: String): Observable<List<CollectionOnlinePayment>>

    fun getOnlinePaymentsCount(businessId: String): Observable<Int>

    fun setOnlinePaymentsDataRead(businessId: String): Completable

    fun listOfNewOnlinePayments(businessId: String): Observable<List<CollectionOnlinePayment>>

    fun lastOnlinePayment(businessId: String): Observable<CollectionOnlinePayment>

    fun getOnlinePaymentsTotalAmount(businessId: String): Observable<Double>

    fun getOnlinePayment(id: String, businessId: String): Observable<CollectionOnlinePayment>

    fun tagMerchantPaymentWithCustomer(customerId: String, paymentId: String, businessId: String): Completable

    fun getLatestOnlinePaymentDate(businessId: String): Observable<DateTime>

    fun isQrMenuEducationShown(): Boolean

    fun setQrMenuEducationShown()

    fun isQrOnlineCollectionEducationShown(): Boolean

    fun setQrOnlineCollectionEducationShown()

    fun isQrSaveSendEducationShown(): Boolean

    fun setQrSaveSendEducationShown()

    fun resetQrEducation(): Completable

    fun canShowQrEducation(): Single<Boolean>

    fun setPaymentOutDestination(
        accountId: String,
        accountType: String,
        paymentType: String,
        paymentAddress: String,
        businessId: String,
    ): Completable

    fun getPaymentOutLinkDetail(
        accountId: String,
        accountType: String,
        businessId: String,
    ): Single<ApiMessages.PaymentOutLinkDetailResponse>

    fun updateGooglePayEnabledForCustomer(customerId: String, enabled: Boolean): Completable

    fun getUnsettledOnlinePaymentAmount(errorCode: String, status: Int, businessId: String): Observable<Double>

    fun triggerMerchantPayout(
        paymentType: String,
        collectionType: String,
        payoutId: String,
        paymentId: String,
        businessId: String,
    ): Completable

    fun setOnlinePaymentStatusLocallyForAllOlderTxn(oldStatus: Int, newStatus: Int, businessId: String): Completable

    fun setOnlinePaymentStatusLocallyForRefundTxn(txnId: String, newStatus: Int): Completable

    fun collectionEvent(customerId: String?, eventName: String, businessId: String): Completable

    fun getOnlinePaymentCount(businessId: String): Observable<Int>

    fun getBlindPayLinkId(accountId: String, businessId: String): Single<ApiMessages.BlindPayCreateLinkResponse>

    fun customerCountWithPaymentIntent(businessId: String): Single<List<String>>

    fun updateCustomerPaymentIntent(customerId: String, paymentIntent: Boolean, businessId: String): Completable

    fun fetchTargetedReferral(businessId: String): Completable

    fun getTargetedReferral(businessId: String): Observable<List<CustomerAdditionalInfo>>

    fun shareTargetedReferral(customerMerchantId: String, businessId: String): Completable

    fun getStatusForTargetedReferralCustomer(customerId: String): Single<Int>

    fun updateReferralLedgerShown(customerId: String): Completable

    fun setOnlinePaymentTag(businessId: String): Completable

    fun getBlindPayShareLink(paymentId: String, businessId: String): Single<ApiMessages.BlindPayShareLinkResponse>

    fun setCashbackBannerClosed(customerId: String, businessId: String): Completable

    fun getCashbackBannerClosed(customerId: String, businessId: String): Single<Boolean>

    suspend fun createBillingItem(inventoryItem: InventoryItem, businessId: String)

    suspend fun getBillingItem(businessId: String): InventoryItemResponse

    suspend fun createBill(listBillItem: List<InventoryItem>, businessId: String): CreateInventoryBillsResponse

    suspend fun getBills(businessId: String): GetInventoryBillsResponse
}
