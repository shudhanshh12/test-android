package `in`.okcredit.merchant.collection.sdk

import `in`.okcredit.collection.contract.*
import `in`.okcredit.merchant.collection.BuildConfig.*
import `in`.okcredit.merchant.collection.CollectionLocalSource
import `in`.okcredit.merchant.collection.CollectionRepositoryImpl
import `in`.okcredit.merchant.collection.CollectionSyncerImpl
import `in`.okcredit.merchant.collection.server.internal.ApiClientRiskV2
import `in`.okcredit.merchant.collection.server.internal.CollectionApiClient
import `in`.okcredit.merchant.collection.server.internal.CollectionBillingApiClient
import `in`.okcredit.merchant.collection.store.CollectionLocalSourceImpl
import `in`.okcredit.merchant.collection.store.database.CollectionDataBase
import `in`.okcredit.merchant.collection.store.database.CollectionDataBaseDao
import `in`.okcredit.merchant.collection.store.database.KycRiskDao
import `in`.okcredit.merchant.collection.usecase.*
import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import android.content.Context
import android.os.Looper
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@dagger.Module
abstract class CollectionModule {

    @Binds
    @AppScope
    abstract fun api(api: CollectionRepositoryImpl): CollectionRepository

    @Binds
    @AppScope
    abstract fun store(store: CollectionLocalSourceImpl): CollectionLocalSource

    @Binds
    @Reusable
    abstract fun syncer(collectionSyncer: CollectionSyncerImpl): CollectionSyncer

    @Binds
    @Reusable
    abstract fun getPaymentOutLinkDetailImpl(getPaymentOutLinkDetailImpl: GetPaymentOutLinkDetailImpl): GetPaymentOutLinkDetail

    @Binds
    @Reusable
    abstract fun setPaymentOutDestinationImpl(setPaymentOutDestinationImpl: SetPaymentOutDestinationImpl): SetPaymentOutDestination

    @Binds
    @Reusable
    abstract fun getBlindPayLinkId(getBlindPayLinkIdImpl: GetBlindPayLinkIdImpl): GetBlindPayLinkId

    @Binds
    @Reusable
    abstract fun fetchPaymentTargetedReferralImpl(fetchPaymentTargetedReferralImpl: FetchPaymentTargetedReferralImpl): FetchPaymentTargetedReferral

    @Binds
    @Reusable
    abstract fun getCustomerAdditionalInfoListImpl(getCustomerAdditionalInfoListImpl: GetCustomerAdditionalInfoListImpl): GetCustomerAdditionalInfoList

    @Binds
    @Reusable
    abstract fun getTargetedReferralInfoListImpl(getTargetedReferralInfoListImpl: GetTargetedReferralInfoListImpl): GetTargetedReferralInfoList

    @Binds
    @Reusable
    abstract fun getTargetedReferralListImpl(getTargetedReferralListImpl: GetTargetedReferralListImpl): GetTargetedReferralList

    @Binds
    @Reusable
    abstract fun shareTargetedReferral(shareTargetedReferralImpl: ShareTargetedReferralImpl): ShareTargetedReferral

    @Binds
    @Reusable
    abstract fun getTargetedReferralInfoSingleCustomerImpl(getTargetedReferralInfoSingleCustomerImpl: GetStatusForTargetedReferralCustomerImpl): GetStatusForTargetedReferralCustomer

    @Binds
    @Reusable
    abstract fun updateCustomerReferralLedgerSeenImpl(updateCustomerReferralLedgerSeenImpl: UpdateCustomerReferralLedgerSeenImpl): UpdateCustomerReferralLedgerSeen

    @Binds
    @Reusable
    abstract fun referralEducationPreferenceImpl(referralEducationPreferenceImpl: ReferralEducationPreferenceImpl): ReferralEducationPreference

    @Binds
    @Reusable
    abstract fun setCashbackBannerClosedImpl(setCashbackBannerClosedImpl: SetCashbackBannerClosedImpl): SetCashbackBannerClosed

    @Binds
    @Reusable
    abstract fun getCashbackBannerClosedImpl(getCashbackBannerClosedImpl: GetCashbackBannerClosedImpl): GetCashbackBannerClosed

    @Binds
    @IntoMap
    @WorkerKey(CollectionSyncerImpl.SyncEverythingWorker::class)
    abstract fun workerSyncEverythingWorker(factory: CollectionSyncerImpl.SyncEverythingWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(CollectionSyncerImpl.SyncCollectionWorker::class)
    abstract fun workerSyncCollectionWorker(factory: CollectionSyncerImpl.SyncCollectionWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @Reusable
    @WorkerKey(CollectionSyncerImpl.SyncCollectionProfileWorker::class)
    abstract fun workerSyncCollectionProfileWorker(factory: CollectionSyncerImpl.SyncCollectionProfileWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @Reusable
    @WorkerKey(CollectionSyncerImpl.SyncMerchantPaymentWorker::class)
    abstract fun workerSyncMerchantWorker(factory: CollectionSyncerImpl.SyncMerchantPaymentWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @Reusable
    @WorkerKey(CollectionSyncerImpl.SyncCollectionProfileWorkerForCustomer::class)
    abstract fun workerSyncCollectionProfileWorkerForCustomer(factory: CollectionSyncerImpl.SyncCollectionProfileWorkerForCustomer.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @Reusable
    @WorkerKey(CollectionSyncerImpl.SyncCollectionProfileWorkerForSupplier::class)
    abstract fun workerSyncCollectionProfileWorkerForSupplier(factory: CollectionSyncerImpl.SyncCollectionProfileWorkerForSupplier.Factory): ChildWorkerFactory

    @Binds
    @Reusable
    abstract fun getBlindPayShareLink(getBlindPayShareLinkImpl: GetBlindPayShareLinkImpl): GetBlindPayShareLink

    @Binds
    @Reusable
    abstract fun isCollectionActivatedOrOnlinePaymentExist(IsCollectionActivatedOrOnlinePaymentExist: IsCollectionActivatedOrOnlinePaymentExistImpl): IsCollectionActivatedOrOnlinePaymentExist

    companion object {

        @Provides
        fun kycDao(db: CollectionDataBase): KycRiskDao {
            return db.kycRiskDao()
        }

        @Provides
        fun database(
            context: Context,
            multipleAccountsDatabaseMigrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): CollectionDataBase {
            return CollectionDataBase.getInstance(context, multipleAccountsDatabaseMigrationHandler)
        }

        @Provides
        @Reusable
        fun dao(database: CollectionDataBase): CollectionDataBaseDao {
            return database.collectionDataBaseDao()
        }

        @Suppress("MemberVisibilityCanBePrivate")
        internal fun checkMainThread() {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                debug {
                    throw IllegalStateException("Initialized on main thread.")
                }
                release {
                    RecordException.recordException(IllegalStateException("Initialized on main thread."))
                }
            }
        }

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): CollectionApiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(COLLECTION_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }

        @Provides
        internal fun apiClientRiskV2(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): ApiClientRiskV2 {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(RISK_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }

        @Provides
        internal fun collectionBillingApiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): CollectionBillingApiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(STAFF_LINK)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
