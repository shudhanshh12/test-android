package `in`.okcredit.merchant.collection

import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.collection.server.CollectionRemoteSource
import `in`.okcredit.merchant.collection.store.database.DbEntityMapper
import `in`.okcredit.merchant.collection.usecase.IsCollectionActivated
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.accounting.contract.model.LedgerType
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val localSource: Lazy<CollectionLocalSource>,
    private val remoteSource: Lazy<CollectionRemoteSource>,
    private val syncer: Lazy<CollectionSyncer>,
    private val isCollectionActivated: Lazy<IsCollectionActivated>,
) : CollectionRepository {

    override fun getCollectionMerchantProfile(businessId: String): Observable<CollectionMerchantProfile> {
        return localSource.get().getCollectionMerchantProfile(businessId)
            .distinctUntilChanged()
            .flatMap {
                if (it.payment_address.isNotNullOrBlank() &&
                    it.merchant_vpa.isNullOrEmpty()
                ) {
                    rxCompletable { syncer.get().executeSyncCollectionProfile() }
                        .andThen(localSource.get().getCollectionMerchantProfile(businessId))
                } else {
                    localSource.get().getCollectionMerchantProfile(businessId)
                }
            }
    }

    override fun getPredictedCollectionMerchantProfile(businessId: String): Single<CollectionMerchantProfile> {
        return rxSingle { remoteSource.get().getPredictedCollectionMerchantProfile(businessId) }
    }

    override fun getCollectionCustomerProfile(
        customer_id: String,
        businessId: String,
    ): Observable<CollectionCustomerProfile> {
        return getGetCollectionForCustomerInternal(customer_id, businessId)
    }

    private fun getGetCollectionForCustomerInternal(
        customer_id: String,
        businessId: String,
    ): Observable<CollectionCustomerProfile> {
        return localSource.get().getCustomerCollectionProfile(customer_id, businessId)
            .distinctUntilChanged()
            .flatMap {
                if (it.qr_intent.isNullOrEmpty()) {
                    rxCompletable { syncer.get().executeSyncCollectionProfileForCustomer(customer_id) }
                        .andThen(localSource.get().getCustomerCollectionProfile(customer_id, businessId))
                } else {
                    localSource.get().getCustomerCollectionProfile(customer_id, businessId)
                }
            }
    }

    override fun listCollectionCustomerProfiles(businessId: String): Observable<List<CollectionCustomerProfile>> {
        return localSource.get().listCustomerCollectionProfiles(businessId)
    }

    override fun listCustomerQrIntents(businessId: String): Observable<Map<String, String?>> {
        return localSource.get().listCustomerQrIntents(businessId)
    }

    companion object {
        const val TAG = "<<<<CollectionSDK"
    }

    override fun listCollections(businessId: String): Observable<List<Collection>> {
        Timber.i("$TAG listCollections inside store executing")
        return localSource.get().listCollections(businessId).observeOn(ThreadUtils.worker())
    }

    override fun getCollectionsOfCustomerOrSupplier(
        customerId: String,
        businessId: String,
    ): Observable<List<Collection>> {
        return localSource.get().listCollectionsOfCustomer(customerId, businessId).observeOn(ThreadUtils.worker())
            .startWith(emptyList<Collection>())
    }

    override fun getCollection(
        collection_id: String,
        businessId: String,
    ): Observable<Collection> {
        return localSource.get().getCollection(collection_id, businessId)
            .observeOn(ThreadUtils.worker())
    }

    override fun clearLocalData(): Completable {
        return localSource.get().clearCollectionSDK()
    }

    override fun setActiveDestination(
        collectionMerchantProfile: CollectionMerchantProfile,
        async: Boolean,
        referralMerchant: String,
        businessId: String,
    ): Single<ApiMessages.MerchantCollectionProfileResponse> {
        return rxSingle {
            remoteSource.get().setActiveDestination(collectionMerchantProfile, async, referralMerchant, businessId)
        }
            .flatMap { profileResponse ->
                // no need to sync merchant profile from server if we are deleting destination ie payment address is empty
                return@flatMap if (collectionMerchantProfile.payment_address.isEmpty()) {
                    localSource.get().clearCollectionMerchantProfile(businessId)
                        .andThen(Single.just(profileResponse))
                } else {
                    rxCompletable { syncer.get().executeSyncCollectionProfile() }
                        .andThen(localSource.get().updatePaymentIntent(false, businessId))
                        .andThen(Single.just(profileResponse))
                }
            }
    }

    override fun validatePaymentAddress(
        payment_address_type: String,
        payment_address: String,
        businessId: String,
    ): Single<Pair<Boolean, String>> {
        return rxSingle { remoteSource.get().validatePaymentAddress(payment_address_type, payment_address, businessId) }
    }

    override fun listCollectionShareInfos(businessId: String): Observable<List<CollectionShareInfo>> {
        return localSource.get().listCollectionShareInfos(businessId)
            .observeOn(ThreadUtils.worker())
    }

    override fun insertCollectionShareInfo(req: CollectionShareInfo, businessId: String): Completable {
        return localSource.get().insertCollectionShareInfo(req, businessId)
            .observeOn(ThreadUtils.worker())
    }

    override fun deleteCollectionShareInfoOfCustomer(customerId: String): Completable {
        return localSource.get().deleteCollectionShareInfoOfCustomer(customerId)
            .observeOn(ThreadUtils.worker())
    }

    override fun isCollectionActivated(): Observable<Boolean> {
        return isCollectionActivated.get().execute()
    }

    override fun customerCountWithPaymentIntent(businessId: String): Single<List<String>> {
        return localSource.get().customerCountWithPaymentIntent(businessId)
    }

    override fun updateCustomerPaymentIntent(
        customerId: String,
        paymentIntent: Boolean,
        businessId: String,
    ): Completable {
        return localSource.get().updatePaymentIntentForCustomer(customerId, paymentIntent)
            .andThen(rxCompletable { remoteSource.get().customerPaymentIntent(businessId, customerId, paymentIntent) })
    }

    override fun createBatchCollection(
        customerIds: List<String>,
        businessId: String,
    ): Completable {
        return rxSingle { remoteSource.get().createBatchCollection(businessId, customerIds, businessId) }
            .flatMapCompletable {
                localSource.get().putCollections(it, businessId)
            }
    }

    override fun getSupplierCollectionProfile(
        accountId: String,
        businessId: String,
    ): Observable<CollectionCustomerProfile> {
        return localSource.get().getSupplierCollectionProfile(accountId, businessId)
    }

    override fun getSupplierPaymentDestination(
        accountId: String,
        businessId: String,
    ): Single<CollectionCustomerProfile> {
        return localSource.get().getSupplierCollectionProfile(accountId, businessId).firstOrError().flatMap { it ->
            if (it.paymentAddress.isNullOrBlank()) {
                return@flatMap rxSingle {
                    remoteSource.get().getCollectionSupplierProfile(accountId, businessId)
                }.flatMap {
                    localSource.get().putSupplierCollectionProfile(it, businessId)
                        .andThen(
                            localSource.get().getSupplierCollectionProfile(accountId, businessId).firstOrError()
                        )
                }
            } else {
                return@flatMap localSource.get().getSupplierCollectionProfile(accountId, businessId).firstOrError()
            }
        }
    }

    override fun enableCustomerPayment(businessId: String): Completable {
        return rxCompletable { remoteSource.get().enableCustomerPayment(businessId) }
    }

    override fun getKycExternalInfo(businessId: String): Observable<KycExternalInfo?> {
        return localSource.get().getCollectionMerchantProfile(businessId).flatMap {
            localSource.get().getKycExternalEntity(it.merchant_id)
        }
    }

    override fun listOnlinePayments(businessId: String): Observable<List<CollectionOnlinePayment>> {
        return localSource.get().listOnlinePayments(businessId)
    }

    override fun getOnlinePaymentsCount(businessId: String): Observable<Int> {
        return localSource.get().getOnlinePaymentsCount(businessId)
    }

    override fun setOnlinePaymentsDataRead(businessId: String): Completable {
        return localSource.get().setOnlinePaymentsDataRead(businessId)
    }

    override fun listOfNewOnlinePayments(businessId: String): Observable<List<CollectionOnlinePayment>> {
        return localSource.get().listOfNewOnlinePayments(businessId)
    }

    override fun lastOnlinePayment(businessId: String): Observable<CollectionOnlinePayment> {
        return localSource.get().lastOnlinePayment(businessId)
    }

    override fun getOnlinePaymentsTotalAmount(businessId: String): Observable<Double> {
        return localSource.get().getOnlinePaymentsTotalAmount(businessId)
    }

    override fun getOnlinePayment(id: String, businessId: String): Observable<CollectionOnlinePayment> {
        return localSource.get().getCollectionOnlinePayment(id, businessId)
    }

    override fun tagMerchantPaymentWithCustomer(
        customerId: String,
        paymentId: String,
        businessId: String,
    ): Completable {
        return rxCompletable { remoteSource.get().tagMerchantPaymentWithCustomer(customerId, paymentId, businessId) }
            .andThen(localSource.get().tagCustomerToPayment(paymentId, customerId, businessId))
    }

    override fun getLatestOnlinePaymentDate(businessId: String): Observable<DateTime> {
        return localSource.get().getLatestOnlinePaymentDate(businessId)
    }

    override fun isQrMenuEducationShown(): Boolean {
        return localSource.get().isQrMenuEducationShown()
    }

    override fun setQrMenuEducationShown() {
        return localSource.get().setQrMenuEducationShown()
    }

    override fun isQrOnlineCollectionEducationShown(): Boolean {
        return localSource.get().isQrOnlineCollectionEducationShown()
    }

    override fun setQrOnlineCollectionEducationShown() {
        return localSource.get().setQrOnlineCollectionEducationShown()
    }

    override fun isQrSaveSendEducationShown(): Boolean {
        return localSource.get().isQrSaveSendEducationShown()
    }

    override fun setQrSaveSendEducationShown() {
        return localSource.get().setQrSaveSendEducationShown()
    }

    override fun resetQrEducation(): Completable {
        return localSource.get().resetQrEducation()
    }

    override fun canShowQrEducation(): Single<Boolean> {
        return localSource.get().canShowQrEducation()
    }

    override fun getKycStatus(businessId: String): Observable<String> {
        return localSource.get().getCollectionMerchantProfile(businessId).flatMap {
            localSource.get().getKycStatus(it.merchant_id)
        }
    }

    override fun setPaymentOutDestination(
        accountId: String,
        accountType: String,
        paymentType: String,
        paymentAddress: String,
        businessId: String,
    ): Completable {
        // Kept a check for account type as for supplier we need save it in DB but for customer we not saving
        // in DB and using response on the fly
        return if (accountType == LedgerType.SUPPLIER.value) {
            rxCompletable {
                remoteSource.get()
                    .setPaymentOutDestination(accountId, accountType, paymentType, paymentAddress, businessId)
            }.andThen(
                localSource.get().putCustomerCollectionProfile(
                    CollectionCustomerProfile(
                        accountId = accountId,
                        paymentAddress = paymentAddress,
                        type = paymentType
                    ),
                    businessId
                )
            ).andThen(rxCompletable { syncer.get().executeSyncCollectionProfileForSupplier(accountId) })
        } else {
            rxCompletable {
                remoteSource.get()
                    .setPaymentOutDestination(accountId, accountType, paymentType, paymentAddress, businessId)
            }
        }
    }

    override fun getPaymentOutLinkDetail(
        accountId: String,
        accountType: String,
        businessId: String,
    ): Single<ApiMessages.PaymentOutLinkDetailResponse> {
        return rxSingle { remoteSource.get().getPaymentOutLinkDetail(accountId, accountType, businessId) }
    }

    override fun collectionEvent(customerId: String?, eventName: String, businessId: String): Completable {
        return rxCompletable { remoteSource.get().collectionEvent(customerId, eventName, businessId) }
    }

    override fun updateGooglePayEnabledForCustomer(customerId: String, enabled: Boolean): Completable {
        return localSource.get().updateGooglePayEnabledForCustomer(customerId, enabled)
    }

    override fun getUnsettledOnlinePaymentAmount(
        errorCode: String,
        status: Int,
        businessId: String,
    ): Observable<Double> {
        return localSource.get().getUnsettledOnlinePaymentAmount(errorCode, status, businessId)
    }

    override fun triggerMerchantPayout(
        paymentType: String,
        collectionType: String,
        payoutId: String,
        paymentId: String,
        businessId: String,
    ): Completable {
        return rxCompletable {
            remoteSource.get().triggerMerchantPayout(paymentType, collectionType, payoutId, paymentId, businessId)
        }
    }

    override fun setOnlinePaymentStatusLocallyForAllOlderTxn(
        oldStatus: Int,
        newStatus: Int,
        businessId: String,
    ): Completable {
        return localSource.get().setOnlinePaymentStatusLocallyForAllOlderTxn(oldStatus, newStatus, businessId)
    }

    override fun setOnlinePaymentStatusLocallyForRefundTxn(txnId: String, newStatus: Int): Completable {
        return localSource.get().setOnlinePaymentStatusLocallyForRefundTxn(txnId, newStatus)
    }

    override fun getOnlinePaymentCount(businessId: String): Observable<Int> {
        return localSource.get().getOnlinePaymentsCount(businessId)
    }

    override fun getBlindPayLinkId(
        accountId: String,
        businessId: String,
    ): Single<ApiMessages.BlindPayCreateLinkResponse> {
        return rxSingle { remoteSource.get().getBlindPayLinkId(accountId, businessId) }
    }

    override fun fetchTargetedReferral(businessId: String): Completable {
        return rxSingle { remoteSource.get().getTargetedReferrals(businessId) }
            .flatMapCompletable {
                localSource.get()
                    .saveCustomerAdditionalInfo(DbEntityMapper.convertToCustomerAdditionalInfoEntity(it, businessId))
            }
    }

    override fun getTargetedReferral(businessId: String): Observable<List<CustomerAdditionalInfo>> {
        return localSource.get().getCustomerAdditionalInfoList(businessId)
    }

    override fun shareTargetedReferral(customerMerchantId: String, businessId: String): Completable {
        return rxCompletable { remoteSource.get().shareTargetedReferral(customerMerchantId, businessId) }
    }

    override fun getStatusForTargetedReferralCustomer(customerId: String): Single<Int> {
        return localSource.get().getStatusForTargetedReferralCustomer(customerId)
    }

    override fun updateReferralLedgerShown(customerId: String): Completable {
        return localSource.get().updateReferralLedgerShown(customerId)
    }

    override fun setOnlinePaymentTag(businessId: String): Completable {
        return Single.fromCallable {
            val lastSync = localSource.get().getLastSyncOnlineCollectionsTime(businessId)
            lastSync?.millis?.div(1000) ?: 0L
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .flatMapCompletable {
                rxCompletable { remoteSource.get().setPaymentTag(it, businessId) }
            }
    }

    override fun getBlindPayShareLink(
        paymentId: String,
        businessId: String,
    ): Single<ApiMessages.BlindPayShareLinkResponse> {
        return rxSingle { remoteSource.get().getBlindPayShareLink(paymentId, businessId) }
    }

    override fun setCashbackBannerClosed(customerId: String, businessId: String) =
        localSource.get().setCashbackBannerClosed(customerId, businessId)

    override fun getCashbackBannerClosed(customerId: String, businessId: String) =
        localSource.get().getCashbackBannerClosed(customerId, businessId)

    override suspend fun createBillingItem(inventoryItem: InventoryItem, businessId: String) {
        remoteSource.get()
            .createBillingItem(inventoryItem.copy(merchantId = businessId), businessId)
    }

    override suspend fun getBillingItem(businessId: String): InventoryItemResponse {
        return remoteSource.get()
            .getBillingItems(businessId)
    }

    override suspend fun createBill(
        listBillItem: List<InventoryItem>,
        businessId: String,
    ): CreateInventoryBillsResponse {
        return remoteSource.get()
            .createBill(listBillItem, businessId)
    }

    override suspend fun getBills(businessId: String): GetInventoryBillsResponse {
        return remoteSource.get()
            .getBills(businessId)
    }
}
