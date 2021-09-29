package `in`.okcredit.merchant.collection

import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.collection.store.database.CustomerAdditionalInfoEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime

interface CollectionLocalSource {

    fun getLastSyncOnlineCollectionsTime(businessId: String): DateTime?

    fun getLastSyncCustomerCollectionsTime(businessId: String): Single<Long>

    fun getLastSyncSupplierCollectionsTime(businessId: String): Single<Long>

    fun setLastSyncCustomerCollectionsTime(time: Long, businessId: String): Completable

    fun setLastSyncSupplierCollectionsTime(time: Long, businessId: String): Completable

    fun listCollections(businessId: String): Observable<List<Collection>>

    fun listCollectionsOfCustomer(customerId: String, businessId: String): Observable<List<Collection>>

    fun getCollection(collectionId: String, businessId: String): Observable<Collection>

    fun putCollections(collections: List<Collection>, businessId: String): Completable

    fun putCollection(collection: Collection, businessId: String): Completable

    fun clearCollectionSDK(): Completable

    fun getCollectionMerchantProfile(businessId: String): Observable<CollectionMerchantProfile>

    fun setCollectionMerchantProfile(collectionMerchantProfile: CollectionMerchantProfile): Completable

    fun clearCollectionMerchantProfile(businessId: String): Completable

    fun listCustomerCollectionProfiles(businessId: String): Observable<List<CollectionCustomerProfile>>

    fun putCustomerCollectionProfiles(collections: List<CollectionCustomerProfile>, businessId: String): Completable

    fun putCustomerCollectionProfile(collectionCusProfile: CollectionCustomerProfile, businessId: String): Completable

    fun getCustomerCollectionProfile(customerId: String, businessId: String): Observable<CollectionCustomerProfile>

    fun listSupplierCollectionProfiles(businessId: String): Observable<List<CollectionCustomerProfile>>

    fun putSupplierCollectionProfiles(collections: List<CollectionCustomerProfile>, businessId: String): Completable

    fun putSupplierCollectionProfile(collectionCusProfile: CollectionCustomerProfile, businessId: String): Completable

    fun getSupplierCollectionProfile(customerId: String, businessId: String): Observable<CollectionCustomerProfile>

    fun listCollectionShareInfos(businessId: String): Observable<List<CollectionShareInfo>>

    fun insertCollectionShareInfo(collectionShareInfo: CollectionShareInfo, businessId: String): Completable

    fun deleteCollectionShareInfoOfCustomer(customerId: String): Completable

    fun getPredictedCollectionMerchantProfile(businessId: String): Observable<CollectionMerchantProfile>

    fun saveKycExternal(kycExternalResponse: KycExternalInfo): Completable

    fun getKycExternalEntity(merchantId: String): Observable<KycExternalInfo?>

    fun getKycStatus(merchantId: String): Observable<String>

    fun insertCollectionOnlinePayments(onlinePayments: List<CollectionOnlinePayment>, businessId: String): Completable

    fun insertCollectionOnlinePayment(onlinePayment: CollectionOnlinePayment, businessId: String): Completable

    fun listOnlinePayments(businessId: String): Observable<List<CollectionOnlinePayment>>

    fun isPaymentExistAlready(id: String): Boolean

    fun setOnlinePaymentsDataRead(businessId: String): Completable

    fun listOfNewOnlinePayments(businessId: String): Observable<List<CollectionOnlinePayment>>

    fun lastOnlinePayment(businessId: String): Observable<CollectionOnlinePayment>

    fun getOnlinePaymentsTotalAmount(businessId: String): Observable<Double>

    fun getCollectionOnlinePayment(id: String, businessId: String): Observable<CollectionOnlinePayment>

    fun getLatestOnlinePaymentDate(businessId: String): Observable<DateTime>

    fun tagCustomerToPayment(paymentId: String, customerId: String, businessId: String): Completable

    fun isQrMenuEducationShown(): Boolean

    fun setQrMenuEducationShown()

    fun isQrOnlineCollectionEducationShown(): Boolean

    fun setQrOnlineCollectionEducationShown()

    fun isQrSaveSendEducationShown(): Boolean

    fun setQrSaveSendEducationShown()

    fun resetQrEducation(): Completable

    fun canShowQrEducation(): Single<Boolean>

    fun updateGooglePayEnabledForCustomer(customerId: String, enabled: Boolean): Completable

    fun getUnsettledOnlinePaymentAmount(errorCode: String, status: Int, businessId: String): Observable<Double>

    fun getOnlinePaymentsCount(businessId: String): Observable<Int>

    fun setOnlinePaymentStatusLocallyForAllOlderTxn(oldStatus: Int, newStatus: Int, businessId: String): Completable

    fun setOnlinePaymentStatusLocallyForRefundTxn(txnId: String, newStatus: Int): Completable

    fun customerCountWithPaymentIntent(businessId: String): Single<List<String>>

    fun updatePaymentIntentForCustomer(customerId: String, paymentIntent: Boolean): Completable

    fun updatePaymentIntent(paymentIntent: Boolean, businessId: String): Completable

    fun listCustomerQrIntents(businessId: String): Observable<Map<String, String?>>

    fun saveCustomerAdditionalInfo(customerAdditionalInfoEntity: List<CustomerAdditionalInfoEntity>): Completable

    fun getCustomerAdditionalInfoList(businessId: String): Observable<List<CustomerAdditionalInfo>>

    fun getStatusForTargetedReferralCustomer(customerId: String): Single<Int>

    fun updateReferralLedgerShown(customerId: String): Completable

    fun setCashbackBannerClosed(customerId: String, businessId: String): Completable

    fun getCashbackBannerClosed(customerId: String, businessId: String): Single<Boolean>
}
