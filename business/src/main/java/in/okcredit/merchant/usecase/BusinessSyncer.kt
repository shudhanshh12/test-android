package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.SyncBusiness
import `in`.okcredit.merchant.server.BusinessRemoteServer
import `in`.okcredit.merchant.store.BusinessLocalSource
import `in`.okcredit.merchant.store.database.Business
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BusinessSyncerImpl @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
    private val remoteServer: Lazy<BusinessRemoteServer>,
    private val localSource: Lazy<BusinessLocalSource>,
    private val syncBusiness: Lazy<SyncBusiness>,
) : BusinessSyncer {

    override fun scheduleSyncBusiness(businessId: String): Completable {
        return Completable.fromAction {
            val workName = WORKER_TAG_BASE

            val workRequest = OneTimeWorkRequestBuilder<SyncMerchantWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(workDataOf(BUSINESS_ID to businessId))
                .addTag(WORKER_TAG_BASE)
                .addTag(workName)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5, TimeUnit.MINUTES
                )
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    fun fetchBusiness(businessId: String) = remoteServer.get().getBusiness(businessId)

    fun saveBusiness(business: Business) = localSource.get().saveBusiness(business)

    override fun executeSyncBusiness(businessId: String) = syncBusiness.get().execute(businessId).ignoreElement()

    override fun scheduleSyncBusinessCategoriesAndBusinessTypes(businessId: String): Completable {
        return Completable.fromAction {
            val workName =
                WORKER_TAG_SYNC_MERCHANT_CATEGORIES

            val workRequest = OneTimeWorkRequestBuilder<SyncMerchantCategoriesAndBusinessTypesWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    workDataOf(
                        SyncMerchantCategoriesAndBusinessTypesWorker.BUSINESS_ID to businessId
                    )
                )
                .addTag(WORKER_TAG_SYNC_MERCHANT_CATEGORIES)
                .addTag(workName)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5, TimeUnit.MINUTES
                )
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    class SyncMerchantWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncBusiness: Lazy<SyncBusinessImpl>,
    ) : BaseRxWorker(context, params) {

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)!!
            return syncBusiness(businessId).ignoreElement()
        }

        class Factory @Inject constructor(
            private val syncBusiness: Lazy<SyncBusinessImpl>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncMerchantWorker(context, params, syncBusiness)
            }
        }

        private fun syncBusiness(businessId: String) = syncBusiness.get().execute(businessId)
    }

    class SyncMerchantCategoriesAndBusinessTypesWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val localSource: Lazy<BusinessLocalSource>,
        private val remoteServer: Lazy<BusinessRemoteServer>,
    ) : BaseRxWorker(context, params) {
        companion object {
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            val businessId = inputData.getString(BUSINESS_ID)!!
            return syncMerchantCategoriesAndBusinessTypes(businessId)
        }

        class Factory @Inject constructor(
            private val localSource: Lazy<BusinessLocalSource>,
            private val remoteServer: Lazy<BusinessRemoteServer>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncMerchantCategoriesAndBusinessTypesWorker(context, params, localSource, remoteServer)
            }
        }

        private fun syncMerchantCategoriesAndBusinessTypes(businessId: String): Completable {
            val jobSyncCategories = remoteServer.get().getCategories(businessId)
                .flatMapCompletable {
                    localSource.get().saveCategories(it)
                }

            val jobSyncBusinessTypes = remoteServer.get().getBusinessTypes(businessId)
                .flatMapCompletable {
                    localSource.get().saveBusinessTypes(it)
                }

            return Completable.mergeArray(
                jobSyncCategories,
                jobSyncBusinessTypes
            )
        }
    }

    companion object {
        const val WORKER_TAG_BASE = "merchant"
        const val WORKER_TAG_SYNC_MERCHANT_CATEGORIES = "merchant/syncMerchantCategories"
        const val MERCHANT_PREF_VALUE = "MERCHANT_PREF_VALUE"
        const val BUSINESS_ID = "business_id"
    }
}

interface BusinessSyncer {
    fun scheduleSyncBusiness(businessId: String): Completable

    fun executeSyncBusiness(businessId: String): Completable

    fun scheduleSyncBusinessCategoriesAndBusinessTypes(businessId: String): Completable
}
