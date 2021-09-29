package `in`.okcredit.merchant.collection.store.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime

@Dao
interface CollectionDataBaseDao {

    /*********************** Collections ***********************/
    @Query("SELECT * FROM Collection WHERE businessId = :businessId ORDER BY create_time DESC")
    fun listCollections(businessId: String): Flowable<List<Collection>>

    @Query("SELECT * FROM Collection WHERE customer_id == :customerId AND businessId = :businessId ORDER BY create_time DESC")
    fun listCollectionsOfCustomer(customerId: String, businessId: String): Flowable<List<Collection>>

    @Query("SELECT * FROM Collection WHERE id == :collectionId")
    fun getCollection(collectionId: String): Flowable<Collection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCollections(vararg collection: Collection)

    @Query("UPDATE Collection SET update_time = :updateTime, customer_id = :customerId, status = :status, errorCode = :errorCode WHERE id = :id")
    fun updateCollectionEntity(
        id: String,
        updateTime: DateTime,
        customerId: String,
        status: Int,
        errorCode: String,
    ): Single<Int>

    @Query("DELETE FROM COLLECTION")
    fun deleteAllCollections(): Completable

    /*********************** Collection Profile ***********************/
    @Query("SELECT * FROM CollectionProfile WHERE merchant_id == :merchantId")
    fun getCollectionsProfile(merchantId: String): Flowable<List<CollectionProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setCollectionsProfile(collectionProfile: CollectionProfile): Completable

    @Query("DELETE FROM CollectionProfile")
    fun deleteMerchantProfile(): Completable

    @Query("DELETE FROM CollectionProfile WHERE merchant_id == :businessId")
    fun deleteMerchantProfileForBusinessId(businessId: String): Completable

    /*********************** Customer Collection Profile ***********************/

    @Query("SELECT * FROM CustomerCollectionProfile where customerId = :id")
    fun getCollectionProfile(id: String): Observable<CustomerCollectionProfile>

    @Query("SELECT * FROM CustomerCollectionProfile WHERE businessId = :businessId")
    fun listCollectionCustomerProfiles(businessId: String): Flowable<List<CustomerCollectionProfile>>

    @Query("SELECT customerId as customer_id, qrIntent as qr_intent FROM CustomerCollectionProfile WHERE businessId = :businessId")
    fun listCustomerQrIntents(businessId: String): Observable<List<CustomerWithQrIntent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomerCollectionProfiles(vararg collectionCustomerProfile: CustomerCollectionProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomerCollectionProfile(collectionCustomerProfile: CustomerCollectionProfile)

    @Query("DELETE FROM CustomerCollectionProfile")
    fun deleteAllCollectionCustomerProfiles(): Completable

    @Query("UPDATE CustomerCollectionProfile SET googlePayEnabled = :enabled WHERE customerId = :customerId")
    fun updateGPayEnabled(customerId: String, enabled: Boolean): Completable

    @Query("SELECT customerId FROM CustomerCollectionProfile WHERE paymentIntent = 1 AND businessId = :businessId")
    fun customerCountWithPaymentIntent(businessId: String): Single<List<String>>

    @Query("UPDATE CustomerCollectionProfile SET paymentIntent = :paymentIntent WHERE customerId = :customerId")
    fun updatePaymentIntentForCustomer(customerId: String, paymentIntent: Boolean): Completable

    @Query("UPDATE CustomerCollectionProfile SET paymentIntent = :paymentIntent WHERE businessId = :businessId")
    fun updatePaymentIntent(paymentIntent: Boolean, businessId: String): Completable

    /*********************** Supplier Collection Profile ***********************/

    @Query("SELECT * FROM SupplierCollectionProfile WHERE businessId = :businessId")
    fun listSupplierCollectionProfiles(businessId: String): Flowable<List<SupplierCollectionProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSupplierCollectionProfiles(vararg collectionSupplierProfile: SupplierCollectionProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSupplierCollectionProfile(collectionSupplierProfile: SupplierCollectionProfile)

    @Query("DELETE FROM SupplierCollectionProfile")
    fun deleteAllSupplierCollectionProfiles(): Completable

    /*********************** Collection Share Info ***********************/

    @Query("SELECT * FROM CollectionShareInfo WHERE businessId = :businessId ORDER BY shared_time DESC")
    fun listCollectionShareInfos(businessId: String): Flowable<List<CollectionShareInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCollectionShareInfoItem(vararg collectionShareInfo: CollectionShareInfo)

    @Query("DELETE FROM collectionShareInfo WHERE customer_id = :customerId")
    fun deleteCollectionShareInfoItem(customerId: String)

    /*********************** online payments ***********************/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCollectionOnlinePayments(vararg collectionOnlinePayment: CollectionOnlinePaymentEntity)

    @Query("SELECT * FROM CollectionOnlinePaymentEntity WHERE businessId = :businessId ORDER BY createdTime DESC")
    fun listCollectionOnlinePayments(businessId: String): Flowable<List<CollectionOnlinePaymentEntity>>

    @Query("UPDATE CollectionOnlinePaymentEntity SET read = 1 WHERE read = 0 AND businessId = :businessId")
    fun setOnlinePaymentsDataRead(businessId: String): Completable

    @Query("UPDATE CollectionOnlinePaymentEntity SET updatedTime = :updateTime, accountId = :customerId, status = :status, errorCode = :errorCode WHERE id = :id")
    fun updateOnlinePaymentEntity(
        id: String,
        updateTime: DateTime,
        customerId: String,
        status: Int,
        errorCode: String,
    ): Single<Int>

    @Query("SELECT COUNT(*) FROM CollectionOnlinePaymentEntity WHERE businessId = :businessId")
    fun getOnlinePaymentsCount(businessId: String): Observable<Int>

    @Query("SELECT SUM(amount) FROM CollectionOnlinePaymentEntity WHERE status = 5 AND businessId = :businessId")
    fun getOnlinePaymentsTotalAmount(businessId: String): Observable<Double>

    @Query("SELECT MAX(createdTime) FROM CollectionOnlinePaymentEntity WHERE businessId = :businessId")
    fun getLatestOnlinePaymentDate(businessId: String): Observable<DateTime>

    @Query("SELECT MAX(updatedTime) FROM CollectionOnlinePaymentEntity WHERE businessId = :businessId")
    fun getLastUpdatedOnlinePayment(businessId: String): DateTime?

    @Query("SELECT * FROM CollectionOnlinePaymentEntity WHERE read = 0 AND businessId = :businessId")
    fun listOfNewOnlinePayments(businessId: String): Flowable<List<CollectionOnlinePaymentEntity>>

    @Query("SELECT * FROM CollectionOnlinePaymentEntity WHERE id = :id")
    fun getCollectionOnlinePayment(id: String): Observable<CollectionOnlinePaymentEntity>

    @Query("DELETE FROM CollectionOnlinePaymentEntity")
    fun deleteAllCollectionOnlinePayments(): Completable

    @Query("UPDATE CollectionOnlinePaymentEntity SET accountId = :customerId WHERE id = :paymentId AND businessId = :businessId ")
    fun tagCustomerToPayment(paymentId: String, customerId: String, businessId: String): Completable

    @Query("SELECT * FROM CollectionOnlinePaymentEntity WHERE errorCode = :errorCode and status = :status and businessId = :businessId")
    fun getOnlinePaymentWithErrorCode(
        errorCode: String,
        status: Int,
        businessId: String,
    ): Flowable<List<CollectionOnlinePaymentEntity>>

    @Query("UPDATE CollectionOnlinePaymentEntity SET status = :newStatus WHERE status = :oldStatus AND businessId = :businessId")
    fun setOnlinePaymentStatusLocallyForAllOlderTxn(oldStatus: Int, newStatus: Int, businessId: String): Completable

    @Query("SELECT COUNT(*) FROM CollectionOnlinePaymentEntity WHERE id = :id")
    fun isPaymentExist(id: String): Int

    @Query("SELECT * FROM CollectionOnlinePaymentEntity WHERE businessId = :businessId ORDER BY createdTime DESC LIMIT 1")
    fun lastOnlinePayment(businessId: String): Observable<CollectionOnlinePaymentEntity>

    @Query("UPDATE CollectionOnlinePaymentEntity SET status = :status WHERE id = :txnId ")
    fun setOnlinePaymentStatusLocallyForRefundTxn(txnId: String, status: Int): Completable

    @Query("UPDATE Collection SET status = :newStatus WHERE status = :oldStatus AND businessId = :businessId")
    fun setOnlinePaymentStatusLocallyForAllCollection(oldStatus: Int, newStatus: Int, businessId: String): Completable

    @Query("UPDATE Collection SET status = :status WHERE id = :txnId ")
    fun setOnlinePaymentStatusLocallyForRefundTxnInCollection(txnId: String, status: Int): Completable

    /*********************** Targeted Referral ***********************/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomerAdditionalInfo(vararg customerAdditionalInfo: CustomerAdditionalInfoEntity): Completable

    // list all customer referral with valid status (ReferralStatus.LINK_INVALID == 3)
    @Query("SELECT * FROM CustomerAdditionalInfoEntity WHERE status != 3 AND businessId = :businessId")
    fun getCustomerAdditionalInfoList(businessId: String): Observable<List<CustomerAdditionalInfoEntity>>

    // list all customer referral with valid status (ReferralStatus.LINK_INVALID == 3)
    @Query("SELECT status FROM CustomerAdditionalInfoEntity WHERE status != 3 and id = :customerId")
    fun getStatusForTargetedReferralCustomer(customerId: String): Single<Int>

    // list all customer referral with valid status (ReferralStatus.LINK_INVALID == 3)
    @Query("DELETE FROM CustomerAdditionalInfoEntity")
    fun deleteAllCustomerReferralInfo(): Completable

    @Query("UPDATE CustomerAdditionalInfoEntity SET link = :link,status = :status,amount = :amount,message = :msg,youtubeLink = :youtubeLink,customerMerchantId = :cId WHERE id = :id")
    fun updateCustomerAdditionalInfo(
        id: String,
        link: String,
        status: Int,
        amount: Long,
        msg: String,
        youtubeLink: String,
        cId: String,
    ): Single<Int>

    @Query("UPDATE CustomerAdditionalInfoEntity SET ledgerSeen = 1 WHERE id = :id")
    fun updateReferralLedgerShown(
        id: String,
    ): Completable
}
