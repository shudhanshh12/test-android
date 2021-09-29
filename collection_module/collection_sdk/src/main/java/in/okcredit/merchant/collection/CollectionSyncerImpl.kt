package `in`.okcredit.merchant.collection

import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionServerErrors
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.CollectionSyncer.Companion.COLLECTION_SYNC_TYPE
import `in`.okcredit.collection.contract.CollectionSyncer.Companion.SYNC_ALL
import `in`.okcredit.collection.contract.CollectionSyncer.Companion.SYNC_CUSTOMER_COLLECTIONS
import `in`.okcredit.collection.contract.CollectionSyncer.Companion.SYNC_SUPPLIER_COLLECTIONS
import `in`.okcredit.collection.contract.KycExternalInfo
import `in`.okcredit.merchant.collection.CollectionRepositoryImpl.Companion.TAG
import `in`.okcredit.merchant.collection.server.CollectionRemoteSource
import `in`.okcredit.merchant.collection.server.internal.ApiEntityMapper
import `in`.okcredit.merchant.collection.utils.Utils
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitFirst
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.className
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseCoroutineWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.base.network.ApiError
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import `in`.okcredit.collection.contract.Collection as Collection1

class CollectionSyncerImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val localSource: Lazy<CollectionLocalSource>,
    private val remoteSource: Lazy<CollectionRemoteSource>,
    private val collectionSyncTracker: Lazy<CollectionSyncTracker>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : CollectionSyncer {

    companion object {
        const val WORKER_TAG_BASE = "collection"
        const val WORKER_TAG_SYNC_EVERYTHING = "collection/scheduleSyncEverything"
        const val WORKER_TAG_SYNC_COLLECTION = "collection/syncCollection"
        const val WORKER_TAG_SYNC_COLLECTION_MERCHANT_PROFILE = "collection/syncCollectionProfile"

        const val CUSTOMER_ID = "customer_id"
        const val ACCOUNT_ID = "account_id"
        const val BUSINESS_ID = "business_id"
    }

    /********** Schedule Workers **********/
    override fun scheduleSyncEverything(source: String, businessId: String) {
        Timber.i("$TAG scheduleSyncEverything Scheduling")
        val workName = WORKER_TAG_SYNC_EVERYTHING

        val workRequest = OneTimeWorkRequestBuilder<SyncEverythingWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(
                    SyncEverythingWorker.BUSINESS_ID to businessId
                )
            )
            .addTag(WORKER_TAG_BASE)
            .addTag(WORKER_TAG_SYNC_EVERYTHING)
            .addTag(workName)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .build()
            .enableWorkerLogging()

        workManager.get()
            .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)

        collectionSyncTracker.get().trackScheduleSyncEverything(source)
    }

    override fun scheduleSyncCollections(syncType: Int, source: String, businessId: String) {
        Timber.i("$TAG scheduleSyncCollections Scheduling")

        val workName = WORKER_TAG_SYNC_COLLECTION
        val data = Data.Builder()
            .putInt(COLLECTION_SYNC_TYPE, syncType)
            .putString(SyncCollectionWorker.BUSINESS_ID, businessId)
        val workRequest = OneTimeWorkRequestBuilder<SyncCollectionWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(data.build())
            .addTag(WORKER_TAG_BASE)
            .addTag(WORKER_TAG_SYNC_COLLECTION)
            .addTag(workName)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .build()
            .enableWorkerLogging()

        workManager.get()
            .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest)

        collectionSyncTracker.get().trackScheduleSyncCollections(source, syncType)
    }

    override fun scheduleCollectionProfile(source: String, businessId: String) {
        Timber.i("$TAG SyncCollectionProfileWorker Scheduling")
        val workName = WORKER_TAG_SYNC_COLLECTION_MERCHANT_PROFILE

        val workRequest = OneTimeWorkRequestBuilder<SyncCollectionProfileWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(workDataOf(SyncCollectionProfileWorker.BUSINESS_ID to businessId))
            .addTag(WORKER_TAG_BASE)
            .addTag(workName)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .build()
            .enableWorkerLogging()

        workManager.get()
            .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        collectionSyncTracker.get().trackScheduleSyncMerchantProfile(source)
    }

    override fun scheduleCollectionProfileForCustomer(customerId: String, businessId: String) {
        Timber.i("$TAG scheduleCollectionProfileForCustomerWorker Scheduling")
        val workName = "WORKER_TAG_SYNC_COLLECTION_PROFILE_FOR_SUPPLIER $customerId"

        val workerData: Data = workDataOf(
            CUSTOMER_ID to customerId,
            BUSINESS_ID to businessId
        )

        val workRequest = OneTimeWorkRequestBuilder<SyncCollectionProfileWorkerForCustomer>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(WORKER_TAG_BASE)
            .addTag(workName)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
            .setInputData(workerData)
            .build()
            .enableWorkerLogging()

        workManager.get().schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        collectionSyncTracker.get().trackScheduleSyncCustomerProfile(customerId)
    }

    override fun scheduleCollectionProfileForSupplier(accountId: String, businessId: String) {
        Timber.i("$TAG scheduleCollectionProfileForSupplierWorker Scheduling")
        val workName = "WORKER_TAG_SYNC_COLLECTION_PROFILE_FOR_SUPPLIER $accountId"

        val workerData: Data = workDataOf(
            ACCOUNT_ID to accountId,
            BUSINESS_ID to businessId
        )

        val workRequest = OneTimeWorkRequestBuilder<SyncCollectionProfileWorkerForSupplier>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(WORKER_TAG_BASE)
            .addTag(workName)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .setInputData(workerData)
            .build()
            .enableWorkerLogging()

        workManager.get().schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        collectionSyncTracker.get().trackScheduleSyncSupplierProfile(accountId)
    }

    override suspend fun scheduleSyncOnlinePayments(source: String, businessId: String?) {
        val workName = "WORKER_TAG_SYNC_MERCHANT_PAYMENT"

        val activeBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
        val workRequest = OneTimeWorkRequestBuilder<SyncMerchantPaymentWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(workDataOf(SyncMerchantPaymentWorker.BUSINESS_ID to activeBusinessId))
            .addTag(WORKER_TAG_BASE)
            .addTag(workName)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .build()
            .enableWorkerLogging()

        collectionSyncTracker.get().trackScheduleSyncMerchantPayment(source)
        workManager.get().schedule(workName, Scope.Business(activeBusinessId), ExistingWorkPolicy.REPLACE, workRequest)
    }

    /********** Sync Everything Worker **********/
    class SyncEverythingWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<CollectionSyncer>,
    ) : BaseCoroutineWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business-id"
        }

        override suspend fun doActualWork() {
            syncEverything()
        }

        private suspend fun syncEverything() {
            Timber.i("$TAG scheduleSyncEverything executing")
            val businessId = inputData.getString(BUSINESS_ID)
            syncer.get().executeSyncCustomerCollections(businessId)
            syncer.get().executeSyncSupplierCollections(businessId)
            syncer.get().executeSyncCollectionProfile()
            syncer.get().executeSyncOnlinePayments(businessId)
        }

        class Factory @Inject constructor(private val syncer: Lazy<CollectionSyncer>) : ChildWorkerFactory {
            override fun create(
                context: Context,
                params: WorkerParameters,
            ): ListenableWorker {
                return SyncEverythingWorker(context, params, syncer)
            }
        }
    }

    /********** Collection Worker **********/
    class SyncCollectionWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<CollectionSyncer>,
    ) : BaseCoroutineWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business-id"
        }

        private suspend fun syncCollections(businessId: String?) {
            Timber.i("$TAG scheduleSyncCollections executing")
            when (inputData.getInt(COLLECTION_SYNC_TYPE, SYNC_ALL)) {
                SYNC_CUSTOMER_COLLECTIONS -> syncer.get().executeSyncCustomerCollections(businessId)
                SYNC_SUPPLIER_COLLECTIONS -> syncer.get().executeSyncSupplierCollections(businessId)
                else -> {
                    syncer.get().executeSyncCustomerCollections(businessId)
                    syncer.get().executeSyncSupplierCollections(businessId)
                }
            }
        }

        class Factory @Inject constructor(private val syncer: Lazy<CollectionSyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncCollectionWorker(context, params, syncer)
            }
        }

        override suspend fun doActualWork() {
            val businessId = inputData.getString(BUSINESS_ID)
            syncCollections(businessId)
        }
    }

    /********** Online Payment Sy=ync Worker **********/
    class SyncMerchantPaymentWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<CollectionSyncer>,
    ) : BaseCoroutineWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override suspend fun doActualWork() {
            val businessId = inputData.getString(SyncCollectionProfileWorker.BUSINESS_ID)!!
            syncer.get().executeSyncOnlinePayments(businessId)
        }

        class Factory @Inject constructor(private val syncer: Lazy<CollectionSyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncMerchantPaymentWorker(context, params, syncer)
            }
        }
    }

    /********** Collection Profile Worker **********/
    class SyncCollectionProfileWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<CollectionSyncer>,
    ) : BaseCoroutineWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override suspend fun doActualWork() {
            val businessId = inputData.getString(BUSINESS_ID)!!
            syncer.get().executeSyncCollectionProfile(businessId)
        }

        class Factory @Inject constructor(private val syncer: Lazy<CollectionSyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncCollectionProfileWorker(context, params, syncer)
            }
        }
    }

    /********** Collection Profile Worker **********/
    class SyncCollectionProfileWorkerForCustomer constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<CollectionSyncer>,
    ) : BaseCoroutineWorker(context, params) {

        override suspend fun doActualWork() {
            val customerId = inputData.getString(CUSTOMER_ID)
            val businessId = inputData.getString(BUSINESS_ID)
            if (customerId.isNullOrEmpty()) {
                Timber.e("scheduleCollectionProfileForCustomerWorker missing customerId")
                return
            }

            syncer.get().executeSyncCollectionProfileForCustomer(customerId, businessId)
        }

        class Factory @Inject constructor(private val syncer: Lazy<CollectionSyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncCollectionProfileWorkerForCustomer(context, params, syncer)
            }
        }
    }

    /********** Collection Profile Worker for Supplier **********/
    class SyncCollectionProfileWorkerForSupplier constructor(
        context: Context,
        params: WorkerParameters,
        private val syncer: Lazy<CollectionSyncer>,
    ) : BaseCoroutineWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override suspend fun doActualWork() {
            val accountId = inputData.getString(ACCOUNT_ID)
            val businessId = inputData.getString(BUSINESS_ID)
            if (accountId.isNullOrEmpty()) {
                Timber.e("executeSyncCollectionProfileForSupplier missing accountId")
                return
            }

            syncer.get().executeSyncCollectionProfileForSupplier(accountId, businessId)
        }

        class Factory @Inject constructor(private val syncer: Lazy<CollectionSyncer>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncCollectionProfileWorkerForSupplier(context, params, syncer)
            }
        }
    }

    override suspend fun executeSyncCustomerCollections(businessId: String?) {
        try {
            val activeBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
            val lastSyncTime = localSource.get().getLastSyncCustomerCollectionsTime(activeBusinessId).await()
            collectionSyncTracker.get().trackExecuteSyncCustomerCollections(lastSyncTime)
            val list = remoteSource.get().getCustomerCollections(null, lastSyncTime.div(1000) + 1, activeBusinessId)
            collectionSyncTracker.get().trackCustomerCollectionsSyncSuccess(
                size = list.size,
                ids = list.joinToString { it.id },
                status = list.joinToString { it.status.toString() },
            )
            if (list.isNotEmpty()) {
                val time = list.maxOfOrNull { it.update_time }?.millis ?: System.currentTimeMillis()
                Timber.i("$TAG max time=$time list size = ${list.size}")
                localSource.get().putCollections(list, activeBusinessId).await()
                localSource.get().setLastSyncCustomerCollectionsTime(time, activeBusinessId).await()
            }
        } catch (exception: Exception) {
            collectionSyncTracker.get().trackCustomerCollectionsSyncError(
                name = exception.className,
                message = exception.message
            )
            if (exception !is ApiError) {
                RecordException.recordException(exception)
            }
            Timber.i("$TAG server response error")
            exception.printStackTrace()
        }
    }

    override suspend fun executeSyncSupplierCollections(businessId: String?) {
        try {
            val activeBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
            val lastSyncTime = localSource.get().getLastSyncSupplierCollectionsTime(activeBusinessId).await()
            collectionSyncTracker.get().trackExecuteSyncSupplierCollections(lastSyncTime)
            val list = remoteSource.get().getSupplierCollections(null, lastSyncTime.div(1000) + 1, activeBusinessId)
            collectionSyncTracker.get().trackSupplierCollectionsSyncSuccess(
                size = list.size,
                ids = list.joinToString { it.id },
                status = list.joinToString { it.status.toString() },
            )
            if (list.isNotEmpty()) {
                val time = list.maxOfOrNull { it.update_time }?.millis ?: System.currentTimeMillis()
                localSource.get().putCollections(list, activeBusinessId).await()
                localSource.get().setLastSyncSupplierCollectionsTime(time, activeBusinessId).await()
            }
        } catch (exception: Exception) {
            collectionSyncTracker.get().trackSupplierCollectionsSyncError(
                name = exception.className,
                message = exception.message
            )
            if (exception !is ApiError) {
                RecordException.recordException(exception)
            }
            exception.printStackTrace()
        }
    }

    override suspend fun executeSyncKyc(merchantProfile: CollectionMerchantProfile?, businessId: String) {
        try {
            if (merchantProfile != null) {
                syncKyc(merchantProfile)
            } else {
                syncKyc(localSource.get().getCollectionMerchantProfile(businessId).awaitFirst())
            }
        } catch (exception: Exception) {
        }
    }

    override suspend fun executeSyncOnlinePayments(businessId: String?) {
        try {
            val activeBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
            collectionSyncTracker.get().trackExecuteSyncOnlinePayments()
            val syncTime = localSource.get().getLastSyncOnlineCollectionsTime(activeBusinessId)
            var lastSyncTime = 0L
            if (syncTime != null) {
                lastSyncTime = syncTime.millis.div(1000) + 1
            }
            val onlinePaymentResponse = remoteSource.get().getOnlinePaymentsList(lastSyncTime, activeBusinessId)
            collectionSyncTracker.get().trackOnlinePaymentsSyncSuccess(
                size = onlinePaymentResponse.onlinePayments.size,
                ids = onlinePaymentResponse.onlinePayments.joinToString { it.id },
                status = onlinePaymentResponse.onlinePayments.joinToString { it.status.toString() },
            )
            if (onlinePaymentResponse.onlinePayments.isNullOrEmpty().not()) {
                val list = Utils.mapList(
                    onlinePaymentResponse.onlinePayments,
                    ApiEntityMapper.ONLINE_PAYMENT_MAPPER
                )
                if (list.isNotEmpty()) {
                    localSource.get().insertCollectionOnlinePayments(list, activeBusinessId).await()
                }
            }
        } catch (exception: Exception) {
            collectionSyncTracker.get().trackOnlinePaymentsSyncError(
                name = exception.className,
                message = exception.message
            )
        }
    }

    override suspend fun syncCollectionFromNotification(collection: Collection1, businessId: String) {
        try {
            collectionSyncTracker.get().trackCollectionSyncNotificationReceived(
                id = collection.id,
                status = collection.status,
                customerId = collection.customer_id,
                error = collection.errorCode
            )
            localSource.get().putCollection(collection, businessId).await()
            collectionSyncTracker.get().trackCustomerCollectionsSyncSuccess(
                size = 1,
                ids = collection.id,
                status = collection.status.toString()
            )
        } catch (exception: Exception) {
        }
    }

    override suspend fun syncOnlinePaymentsFromNotification(
        onlinePayment: CollectionOnlinePayment,
        businessId: String,
    ) {
        try {
            collectionSyncTracker.get().trackMerchantPaymentSyncNotificationReceived(
                id = onlinePayment.id,
                amount = onlinePayment.amount,
                status = onlinePayment.status,
                type = onlinePayment.type,
                error = onlinePayment.errorCode
            )
            localSource.get().insertCollectionOnlinePayment(onlinePayment, businessId).await()
            collectionSyncTracker.get().trackOnlinePaymentsSyncSuccess(
                size = 1,
                ids = onlinePayment.id,
                status = onlinePayment.status.toString()
            )
        } catch (exception: Exception) {
        }
    }

    override suspend fun executeSyncCollectionProfile(businessId: String?) {
        collectionSyncTracker.get().trackExecuteSyncMerchantProfile()
        val activeBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
        syncCollectionsInternal(activeBusinessId)
    }

    private suspend fun syncCollectionsInternal(businessId: String) {
        try {
            val response = remoteSource.get().getCollectionProfiles(businessId)
            collectionSyncTracker.get().trackMerchantProfileSyncSuccess(
                merchantVpaPresent = response.collectionMerchantProfile.merchant_vpa.isNotNullOrBlank(),
                paymentAddressPresent = response.collectionMerchantProfile.payment_address.isNotNullOrBlank(),
                size = response.collectionCustomerProfiles.size
            )
            localSource.get().setCollectionMerchantProfile(response.collectionMerchantProfile).await()
            localSource.get().putCustomerCollectionProfiles(response.collectionCustomerProfiles, businessId).await()
            localSource.get().putSupplierCollectionProfiles(response.supplierCollectionProfiles, businessId).await()
            executeSyncKyc(response.collectionMerchantProfile, businessId)
        } catch (exception: Exception) {
            collectionSyncTracker.get().trackMerchantProfileSyncError(
                name = exception.className,
                message = exception.message
            )
            if (exception is CollectionServerErrors.AddressNotFound) {
                localSource.get().clearCollectionMerchantProfile(businessId).await()
            } else {
                if (exception !is ApiError) {
                    RecordException.recordException(exception)
                }
            }
        }
    }

    private suspend fun syncKyc(merchantProfile: CollectionMerchantProfile) {
        try {
            val response = remoteSource.get().getKycRiskAttributes(merchantProfile.merchant_id)
            localSource.get().saveKycExternal(
                KycExternalInfo(
                    merchantId = merchantProfile.merchant_id,
                    kyc = response.kycInfo.kycStatus,
                    upiDailyLimit = response.limitInfo.upiLimit.totalDailyAmountLimit,
                    nonUpiDailyLimit = response.limitInfo.nonUpiLimit.totalDailyAmountLimit,
                    upiDailyTransactionAmount = response.limitInfo.upiLimit.totalDailyLimitUsed,
                    nonUpiDailyTransactionAmount = response.limitInfo.nonUpiLimit.totalDailyLimitUsed,
                    category = response.riskCategory
                )
            ).await()
        } catch (exception: Exception) {
            if (exception !is ApiError) {
                RecordException.recordException(exception)
            }
        }
    }

    override suspend fun executeSyncCollectionProfileForCustomer(customerId: String, businessId: String?) {
        collectionSyncTracker.get().trackExecuteSyncCustomerProfile(customerId)
        val activeBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
        syncCollectionProfileForCustomerInternal(customerId, activeBusinessId)
    }

    private suspend fun syncCollectionProfileForCustomerInternal(customerId: String, businessId: String) {
        try {
            val customerProfile = remoteSource.get().getCollectionCustomerProfile(customerId, businessId)
            collectionSyncTracker.get().trackCustomerProfileSyncSuccess(
                qrIntentPresent = customerProfile.qr_intent.isNotNullOrBlank(),
                customerId = customerId
            )
            localSource.get().putCustomerCollectionProfile(customerProfile, businessId).await()
        } catch (exception: Exception) {
            collectionSyncTracker.get().trackCustomerProfileSyncError(
                customerId = customerId,
                name = exception.className,
                message = exception.message
            )
            if (exception !is ApiError) {
                RecordException.recordException(exception)
            }
        }
    }

    override suspend fun executeSyncCollectionProfileForSupplier(accountId: String, businessId: String?) {
        try {
            val activeBusinessId = getActiveBusinessId.get().thisOrActiveBusinessId(businessId).await()
            collectionSyncTracker.get().trackExecuteSyncSupplierProfile(accountId)
            val supplierProfile = remoteSource.get().getCollectionSupplierProfile(accountId, activeBusinessId)
            collectionSyncTracker.get().trackSupplierProfileSyncSuccess(
                paymentAddressPresent = supplierProfile.paymentAddress.isNotNullOrBlank(),
                accountId = accountId
            )
            localSource.get().putSupplierCollectionProfile(supplierProfile, activeBusinessId).await()
        } catch (exception: Exception) {
            collectionSyncTracker.get().trackSupplierProfileSyncError(exception.className, exception.message, accountId)
            if (exception !is ApiError) {
                RecordException.recordException(exception)
            }
        }
    }
}
