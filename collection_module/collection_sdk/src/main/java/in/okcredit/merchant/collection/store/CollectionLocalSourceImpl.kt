package `in`.okcredit.merchant.collection.store

import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.collection.CollectionLocalSource
import `in`.okcredit.merchant.collection.CollectionRepositoryImpl.Companion.TAG
import `in`.okcredit.merchant.collection.store.database.CollectionDataBaseDao
import `in`.okcredit.merchant.collection.store.database.CollectionOnlinePaymentEntity
import `in`.okcredit.merchant.collection.store.database.CustomerAdditionalInfoEntity
import `in`.okcredit.merchant.collection.store.database.DbEntityMapper
import `in`.okcredit.merchant.collection.store.database.DbEntityMapper.KYC_EXTERNAL_MAPPER
import `in`.okcredit.merchant.collection.store.database.KycRiskDao
import `in`.okcredit.merchant.collection.store.preference.CollectionPreference
import `in`.okcredit.merchant.collection.utils.CommonUtils
import `in`.okcredit.merchant.collection.utils.Utils
import `in`.okcredit.merchant.contract.GetBusinessIdList
import androidx.room.EmptyResultSetException
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import org.jetbrains.annotations.NonNls
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.blockingGetBoolean
import tech.okcredit.android.base.preferences.blockingSet
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import javax.inject.Inject

class CollectionLocalSourceImpl @Inject constructor(
    private val collectionDao: Lazy<CollectionDataBaseDao>,
    private val kycDao: Lazy<KycRiskDao>,
    private val collectionPreference: Lazy<CollectionPreference>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
) : CollectionLocalSource {

    init {
        // set value initially
        runUpdateMigration()
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .subscribe()
    }

    private fun runUpdateMigration() = rxCompletable {
        getBusinessIdList.get().execute().first().forEach { businessId ->
            val time = collectionPreference.get()
                .getLong(KEY_COLLECTION_LAST_SYNC_EVERYTHING_TIME, Scope.Business(businessId))
                .first()
            Timber.i("$TAG updateLastSyncEverythingTime of Collections=$time")
            // migration from combined collections API to separate customer and supplier collections
            if (time > 0) {
                collectionPreference.get().set(KEY_LAST_SYNC_CUSTOMER_COLLECTIONS, time, Scope.Business(businessId))
                collectionPreference.get().set(KEY_LAST_SYNC_SUPPLIER_COLLECTIONS, time, Scope.Business(businessId))
                collectionPreference.get().set(KEY_COLLECTION_LAST_SYNC_EVERYTHING_TIME, 0L, Scope.Business(businessId))
            }
        }
    }

    override fun getLastSyncOnlineCollectionsTime(businessId: String): DateTime? {
        return collectionDao.get().getLastUpdatedOnlinePayment(businessId)
    }

    override fun getLastSyncCustomerCollectionsTime(businessId: String): Single<Long> {
        return collectionPreference.get().getLong(KEY_LAST_SYNC_CUSTOMER_COLLECTIONS, Scope.Business(businessId))
            .asObservable().firstOrError()
    }

    override fun getLastSyncSupplierCollectionsTime(businessId: String): Single<Long> {
        return collectionPreference.get().getLong(KEY_LAST_SYNC_SUPPLIER_COLLECTIONS, Scope.Business(businessId))
            .asObservable().firstOrError()
    }

    override fun setLastSyncCustomerCollectionsTime(time: Long, businessId: String): Completable {
        return rxCompletable {
            collectionPreference.get().set(KEY_LAST_SYNC_CUSTOMER_COLLECTIONS, time, Scope.Business(businessId))
        }.subscribeOn(ThreadUtils.database())
    }

    override fun setLastSyncSupplierCollectionsTime(time: Long, businessId: String): Completable {
        return rxCompletable {
            collectionPreference.get().set(KEY_LAST_SYNC_SUPPLIER_COLLECTIONS, time, Scope.Business(businessId))
        }.subscribeOn(ThreadUtils.database())
    }

    override fun clearCollectionSDK(): Completable {
        return rxCompletable {
            getBusinessIdList.get().execute().first().forEach { businessId ->
                collectionPreference.get().set(KEY_COLLECTION_LAST_SYNC_EVERYTHING_TIME, 0, Scope.Business(businessId))
                collectionPreference.get().set(KEY_LAST_SYNC_CUSTOMER_COLLECTIONS, 0, Scope.Business(businessId))
                collectionPreference.get().set(KEY_LAST_SYNC_SUPPLIER_COLLECTIONS, 0, Scope.Business(businessId))
            }
        }.andThen(collectionDao.get().deleteAllCollections())
            .andThen(collectionDao.get().deleteAllCollectionCustomerProfiles())
            .andThen(collectionDao.get().deleteAllSupplierCollectionProfiles())
            .andThen(collectionDao.get().deleteAllCollectionOnlinePayments())
            .andThen(collectionDao.get().deleteMerchantProfile())
            .andThen(collectionPreference.get().clearCollectionPref())
            .andThen(collectionDao.get().deleteAllCustomerReferralInfo())
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    /********************************* Collections *********************************/
    override fun listCollections(businessId: String): Observable<List<Collection>> {
        Timber.i("$TAG executing listCollections from collectionDao store")
        return collectionDao.get().listCollections(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map {
                CommonUtils.mapList(it, DbEntityMapper.COLLECTION(businessId).reverse())
            }
            .toObservable()
    }

    override fun listCollectionsOfCustomer(customerId: String, businessId: String): Observable<List<Collection>> {
        Timber.i("$TAG executing listCollections from collectionDao store")
        return collectionDao.get().listCollectionsOfCustomer(customerId, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map {
                CommonUtils.mapList(it, DbEntityMapper.COLLECTION(businessId).reverse())
            }
            .toObservable()
    }

    override fun getCollection(collectionId: String, businessId: String): Observable<Collection> {
        Timber.i("$TAG executing listCollections from collectionDao store")
        return collectionDao.get().getCollection(collectionId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map<Collection> { collection ->
                DbEntityMapper.COLLECTION(businessId).reverse().convert(collection)
            }
            .toObservable()
    }

    override fun putCollections(collections: List<Collection>, businessId: String): Completable {
        Timber.i("$TAG executing putCollections from collectionDao store")
        return Completable.fromAction {
            val list = Utils.mapList(collections, DbEntityMapper.COLLECTION(businessId))
                .toTypedArray<`in`.okcredit.merchant.collection.store.database.Collection>()
            collectionDao.get().insertCollections(*list)
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun putCollection(collection: Collection, businessId: String): Completable {
        return collectionDao.get().updateCollectionEntity(
            id = collection.id,
            updateTime = collection.update_time,
            customerId = collection.customer_id,
            status = collection.status,
            errorCode = collection.errorCode
        ).flatMapCompletable {
            return@flatMapCompletable if (it == 0) {
                Completable.fromAction {
                    collectionDao.get().insertCollections(DbEntityMapper.COLLECTION(businessId).convert(collection)!!)
                }
            } else {
                Completable.complete()
            }
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    /********************************* Collection Profile *********************************/
    override fun getCollectionMerchantProfile(businessId: String): Observable<CollectionMerchantProfile> {
        return collectionDao.get().getCollectionsProfile(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map<CollectionMerchantProfile> { collection_profile ->
                if (collection_profile.isNotEmpty()) {
                    DbEntityMapper.collectionMerchantProfile.reverse().convert(collection_profile[0])
                } else {
                    CollectionMerchantProfile.empty()
                }
            }
            .toObservable()
    }

    override fun customerCountWithPaymentIntent(businessId: String): Single<List<String>> {
        return collectionDao.get().customerCountWithPaymentIntent(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun updatePaymentIntentForCustomer(customerId: String, paymentIntent: Boolean): Completable {
        return collectionDao.get().updatePaymentIntentForCustomer(customerId, paymentIntent)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun updatePaymentIntent(paymentIntent: Boolean, businessId: String): Completable {
        return collectionDao.get().updatePaymentIntent(paymentIntent, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getPredictedCollectionMerchantProfile(businessId: String): Observable<CollectionMerchantProfile> {
        return getCollectionMerchantProfile(businessId) // TODO what to do here
    }

    override fun setCollectionMerchantProfile(collectionMerchantProfile: CollectionMerchantProfile): Completable {
        return collectionDao.get().setCollectionsProfile(
            DbEntityMapper.collectionMerchantProfile.convert(
                collectionMerchantProfile
            )!!
        )
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun clearCollectionMerchantProfile(businessId: String): Completable {
        return collectionDao.get().deleteMerchantProfileForBusinessId(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    /********************************* Collection Customer Profile *********************************/
    override fun listCustomerCollectionProfiles(businessId: String): Observable<List<CollectionCustomerProfile>> {
        Timber.i("$TAG executing listCollectionCustomerProfiles from collectionDao store")
        return collectionDao.get().listCollectionCustomerProfiles(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map {
                CommonUtils.mapList(it, DbEntityMapper.COLLECTION_CUSTOMER_PROFILE(businessId).reverse())
            }
            .toObservable()
    }

    override fun listCustomerQrIntents(businessId: String): Observable<Map<String, String?>> {
        return collectionDao.get().listCustomerQrIntents(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { list -> list.associate { it.customer_id to it.qr_intent } }
    }

    override fun putCustomerCollectionProfiles(
        collections: List<CollectionCustomerProfile>,
        businessId: String,
    ): Completable {
        Timber.i("$TAG executing putCollectionCustomerProfiles from collectionDao store")
        return Completable.fromAction {
            val list = Utils.mapList(collections, DbEntityMapper.COLLECTION_CUSTOMER_PROFILE(businessId))
                .toTypedArray<`in`.okcredit.merchant.collection.store.database.CustomerCollectionProfile>()
            collectionDao.get().insertCustomerCollectionProfiles(*list)
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun putCustomerCollectionProfile(
        collectionCusProfile: CollectionCustomerProfile,
        businessId: String,
    ): Completable {
        Timber.i("$TAG executing putCollectionCustomerProfiles from collectionDao store")
        return Completable.fromAction {
            collectionDao.get().insertCustomerCollectionProfile(
                DbEntityMapper.COLLECTION_CUSTOMER_PROFILE(businessId).convert(collectionCusProfile)!!
            )
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getCustomerCollectionProfile(
        customerId: String,
        businessId: String,
    ): Observable<CollectionCustomerProfile> {
        return collectionDao.get().listCollectionCustomerProfiles(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { customerProfiles ->
                var collectionCustomerProfile = CollectionCustomerProfile(
                    accountId = customerId,
                    message_link = null,
                    message = null,
                    link_intent = null,
                    qr_intent = null,
                    show_image = false,
                    fromMerchantPaymentLink = null,
                    fromMerchantUpiIntent = null
                )
                customerProfiles.map {
                    if (it.customerId == customerId) {
                        collectionCustomerProfile =
                            DbEntityMapper.COLLECTION_CUSTOMER_PROFILE(businessId).reverse().convert(it)!!
                    }
                }
                return@map collectionCustomerProfile
            }
            .distinctUntilChanged()
            .toObservable()
    }

    override fun listSupplierCollectionProfiles(businessId: String): Observable<List<CollectionCustomerProfile>> {
        return collectionDao.get().listSupplierCollectionProfiles(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map {
                CommonUtils.mapList(it, DbEntityMapper.SUPPLIER_COLLECTION_PROFILE(businessId).reverse())
            }
            .toObservable()
    }

    override fun putSupplierCollectionProfiles(
        collections: List<CollectionCustomerProfile>,
        businessId: String,
    ): Completable {
        return Completable.fromAction {
            val list = Utils.mapList(collections, DbEntityMapper.SUPPLIER_COLLECTION_PROFILE(businessId))
                .toTypedArray<`in`.okcredit.merchant.collection.store.database.SupplierCollectionProfile>()
            collectionDao.get().insertSupplierCollectionProfiles(*list)
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun putSupplierCollectionProfile(
        collectionCusProfile: CollectionCustomerProfile,
        businessId: String,
    ): Completable {
        return Completable.fromAction {
            collectionDao.get().insertSupplierCollectionProfile(
                DbEntityMapper.SUPPLIER_COLLECTION_PROFILE(businessId).convert(collectionCusProfile)!!
            )
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getSupplierCollectionProfile(
        customerId: String,
        businessId: String,
    ): Observable<CollectionCustomerProfile> {
        return collectionDao.get().listSupplierCollectionProfiles(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { customerProfiles ->
                var collectionCustomerProfile = CollectionCustomerProfile(
                    accountId = customerId,
                    message_link = null,
                    message = null,
                    link_intent = null,
                    qr_intent = null,
                    show_image = false,
                    fromMerchantPaymentLink = null,
                    fromMerchantUpiIntent = null
                )
                customerProfiles.map {
                    if (it.accountId == customerId) {
                        collectionCustomerProfile =
                            DbEntityMapper.SUPPLIER_COLLECTION_PROFILE(businessId).reverse().convert(it)!!
                    }
                }
                return@map collectionCustomerProfile
            }
            .distinctUntilChanged()
            .toObservable()
    }

    /********************************* Collection Shared Infos *********************************/

    override fun insertCollectionShareInfo(collectionShareInfo: CollectionShareInfo, businessId: String): Completable {
        return Completable
            .fromAction {
                collectionDao.get().insertCollectionShareInfoItem(
                    DbEntityMapper.COLLECTION_SHARE_INFO(businessId).convert(
                        collectionShareInfo
                    )!!
                )
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun deleteCollectionShareInfoOfCustomer(customerId: String): Completable {
        return Completable.fromAction {
            collectionDao.get().deleteCollectionShareInfoItem(customerId)
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun listCollectionShareInfos(businessId: String): Observable<List<CollectionShareInfo>> {
        return collectionDao.get().listCollectionShareInfos(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { collectionSharedInfo ->
                Utils.mapList(collectionSharedInfo, DbEntityMapper.COLLECTION_SHARE_INFO(businessId).reverse())
            }
            .toObservable()
    }

    override fun saveKycExternal(
        kycExternalResponse: KycExternalInfo,
    ): Completable {
        return kycDao.get().deleteKycData(kycExternalResponse.merchantId).andThen(
            kycDao.get().insertKycExternal(
                KYC_EXTERNAL_MAPPER.convert(kycExternalResponse)!!
            )
        ).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getKycExternalEntity(merchantId: String): Observable<KycExternalInfo?> {
        return kycDao.get().getKycExternalEntity(merchantId).map {
            KYC_EXTERNAL_MAPPER.reverse().convert(it)
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getKycStatus(merchantId: String): Observable<String> {
        return kycDao.get().getKycStatus(merchantId)
    }

    override fun insertCollectionOnlinePayments(
        onlinePayments: List<CollectionOnlinePayment>,
        businessId: String,
    ): Completable {
        return Completable.fromAction {
            val list = Utils.mapList(onlinePayments, DbEntityMapper.ONLINE_PAYMENTS_MAPPER(businessId))
                .toTypedArray<CollectionOnlinePaymentEntity>()
            collectionDao.get().insertCollectionOnlinePayments(*list)
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun insertCollectionOnlinePayment(
        onlinePayment: CollectionOnlinePayment,
        businessId: String,
    ): Completable {
        return collectionDao.get().updateOnlinePaymentEntity(
            id = onlinePayment.id,
            updateTime = onlinePayment.updatedTime,
            customerId = onlinePayment.accountId,
            status = onlinePayment.status,
            errorCode = onlinePayment.errorCode
        ).flatMapCompletable {
            if (it == 0) {
                Completable.fromAction {
                    collectionDao.get().insertCollectionOnlinePayments(
                        DbEntityMapper.ONLINE_PAYMENTS_MAPPER(businessId).convert(onlinePayment)!!
                    )
                }
            } else {
                Completable.complete()
            }
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun listOnlinePayments(businessId: String): Observable<List<CollectionOnlinePayment>> {
        return collectionDao.get().listCollectionOnlinePayments(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { onlinePayments ->
                Utils.mapList(onlinePayments, DbEntityMapper.ONLINE_PAYMENTS_MAPPER(businessId).reverse())
            }
            .toObservable()
    }

    override fun isPaymentExistAlready(id: String): Boolean {
        return collectionDao.get().isPaymentExist(id) != 0
    }

    override fun setOnlinePaymentsDataRead(businessId: String): Completable {
        return collectionDao.get().setOnlinePaymentsDataRead(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun listOfNewOnlinePayments(businessId: String): Observable<List<CollectionOnlinePayment>> {
        return collectionDao.get().listOfNewOnlinePayments(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { onlinePayments ->
                Utils.mapList(onlinePayments, DbEntityMapper.ONLINE_PAYMENTS_MAPPER(businessId).reverse())
            }
            .toObservable()
    }

    override fun lastOnlinePayment(businessId: String): Observable<CollectionOnlinePayment> {
        return collectionDao.get().lastOnlinePayment(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { onlinePayments ->
                DbEntityMapper.ONLINE_PAYMENTS_MAPPER(businessId).reverse().convert(onlinePayments)
            }
    }

    override fun getOnlinePaymentsTotalAmount(businessId: String): Observable<Double> {
        return collectionDao.get().getOnlinePaymentsTotalAmount(businessId)
    }

    override fun getCollectionOnlinePayment(id: String, businessId: String): Observable<CollectionOnlinePayment> {
        return collectionDao.get().getCollectionOnlinePayment(id)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { onlinePayment ->
                Utils.mapItem(onlinePayment, DbEntityMapper.ONLINE_PAYMENTS_MAPPER(businessId).reverse())
            }
    }

    override fun getLatestOnlinePaymentDate(businessId: String): Observable<DateTime> {
        return collectionDao.get().getLatestOnlinePaymentDate(businessId)
    }

    override fun tagCustomerToPayment(paymentId: String, customerId: String, businessId: String): Completable {
        return collectionDao.get().tagCustomerToPayment(paymentId, customerId, businessId)
    }

    override fun isQrMenuEducationShown(): Boolean {
        return collectionPreference.get().blockingGetBoolean(QR_MENU_EDUCATION_SHOWN, Scope.Individual)
    }

    override fun setQrMenuEducationShown() {
        collectionPreference.get().blockingSet(QR_MENU_EDUCATION_SHOWN, true, Scope.Individual)
    }

    override fun isQrOnlineCollectionEducationShown(): Boolean {
        return collectionPreference.get().blockingGetBoolean(QR_ONLINE_COLLECTION_EDUCATION_SHOWN, Scope.Individual)
    }

    override fun setQrOnlineCollectionEducationShown() {
        collectionPreference.get().blockingSet(QR_ONLINE_COLLECTION_EDUCATION_SHOWN, true, Scope.Individual)
    }

    override fun isQrSaveSendEducationShown(): Boolean {
        return collectionPreference.get().blockingGetBoolean(QR_SAVE_SEND_EDUCATION_SHOWN, Scope.Individual)
    }

    override fun setQrSaveSendEducationShown() {
        collectionPreference.get().blockingSet(QR_SAVE_SEND_EDUCATION_SHOWN, true, Scope.Individual)
    }

    override fun resetQrEducation() = rxCompletable {
        collectionPreference.get().remove(QR_ONLINE_COLLECTION_EDUCATION_SHOWN, Scope.Individual)
        collectionPreference.get().remove(QR_MENU_EDUCATION_SHOWN, Scope.Individual)
        collectionPreference.get().remove(QR_SAVE_SEND_EDUCATION_SHOWN, Scope.Individual)
        collectionPreference.get().set(QR_EDUCATION_CAN_SHOW, true, Scope.Individual)
    }

    override fun canShowQrEducation(): Single<Boolean> {
        return collectionPreference.get().getBoolean(QR_EDUCATION_CAN_SHOW, Scope.Individual)
            .asObservable().firstOrError()
    }

    override fun updateGooglePayEnabledForCustomer(customerId: String, enabled: Boolean): Completable {
        return collectionDao.get().updateGPayEnabled(customerId, enabled)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getUnsettledOnlinePaymentAmount(
        errorCode: String,
        status: Int,
        businessId: String,
    ): Observable<Double> {
        return collectionDao.get().getOnlinePaymentWithErrorCode(errorCode, status, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .toObservable()
            .map {
                var sum = 0.0
                it.forEach { entity ->
                    if (errorCode == entity.errorCode)
                        sum += entity.amount
                }
                sum
            }
    }

    override fun getOnlinePaymentsCount(businessId: String): Observable<Int> {
        return collectionDao.get().getOnlinePaymentsCount(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun setOnlinePaymentStatusLocallyForAllOlderTxn(
        oldStatus: Int,
        newStatus: Int,
        businessId: String,
    ): Completable {
        return collectionDao.get().setOnlinePaymentStatusLocallyForAllOlderTxn(oldStatus, newStatus, businessId)
            .andThen(
                collectionDao.get()
                    .setOnlinePaymentStatusLocallyForAllCollection(oldStatus, newStatus, businessId)
            )
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun setOnlinePaymentStatusLocallyForRefundTxn(txnId: String, newStatus: Int): Completable {
        return collectionDao.get().setOnlinePaymentStatusLocallyForRefundTxn(txnId, newStatus)
            .andThen(collectionDao.get().setOnlinePaymentStatusLocallyForRefundTxnInCollection(txnId, newStatus))
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun saveCustomerAdditionalInfo(customerAdditionalInfoEntity: List<CustomerAdditionalInfoEntity>): Completable {
        return Completable.fromAction {
            customerAdditionalInfoEntity.forEach {
                collectionDao.get().updateCustomerAdditionalInfo(
                    id = it.id,
                    link = it.link,
                    status = it.status,
                    amount = it.amount,
                    msg = it.message,
                    youtubeLink = it.youtubeLink,
                    cId = it.customerMerchantId
                )
                    .subscribeOn(ThreadUtils.database())
                    .observeOn(ThreadUtils.worker())
                    .flatMapCompletable { rowCount ->
                        if (rowCount == 0) {
                            collectionDao.get().insertCustomerAdditionalInfo(it)
                                .subscribeOn(ThreadUtils.database())
                                .observeOn(ThreadUtils.worker())
                        } else Completable.complete()
                    }.subscribe()
            }
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun updateReferralLedgerShown(customerId: String): Completable {
        return collectionDao.get().updateReferralLedgerShown(customerId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getCustomerAdditionalInfoList(businessId: String): Observable<List<CustomerAdditionalInfo>> {
        return collectionDao.get().getCustomerAdditionalInfoList(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map {
                val customerAdditionalInfo: ArrayList<CustomerAdditionalInfo> = arrayListOf()
                it.forEach { entity ->
                    customerAdditionalInfo.add(DbEntityMapper.convertToCustomerAdditionalInfo(entity))
                }
                customerAdditionalInfo
            }
    }

    override fun getStatusForTargetedReferralCustomer(customerId: String): Single<Int> {
        return collectionDao.get().getStatusForTargetedReferralCustomer(customerId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .onErrorReturn {
                return@onErrorReturn if (it is EmptyResultSetException) {
                    ReferralStatus.DEFAULT.value
                } else {
                    throw it
                }
            }
    }

    override fun setCashbackBannerClosed(customerId: String, businessId: String) =
        collectionPreference.get().setCashbackBannerClosed(customerId, businessId)

    override fun getCashbackBannerClosed(customerId: String, businessId: String) =
        collectionPreference.get().getCashbackBannerClosed(customerId, businessId)

    companion object {
        @NonNls
        internal const val KEY_COLLECTION_LAST_SYNC_EVERYTHING_TIME = "KEY_COLLECTION_LAST_SYNC_EVERYTHING_TIME"
        internal const val KEY_LAST_SYNC_CUSTOMER_COLLECTIONS = "KEY_LAST_SYNC_CUSTOMER_COLLECTIONS"
        internal const val KEY_LAST_SYNC_SUPPLIER_COLLECTIONS = "KEY_LAST_SYNC_SUPPLIER_COLLECTIONS"

        const val QR_ONLINE_COLLECTION_EDUCATION_SHOWN = "qr_online_collection_education_shown"
        const val QR_MENU_EDUCATION_SHOWN = "qr_menu_education_shown"
        const val QR_SAVE_SEND_EDUCATION_SHOWN = "qr_save_send_education_shown"
        const val QR_EDUCATION_CAN_SHOW = "qr_education_can_show"
    }
}
