package `in`.okcredit.merchant.sdk

import `in`.okcredit.merchant.BusinessNavigatorImpl
import `in`.okcredit.merchant.BusinessRepositoryImpl
import `in`.okcredit.merchant.contract.*
import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.merchant.BuildConfig
import `in`.okcredit.merchant.migration.MultipleAccountsDatabaseMigrationHandlerImpl
import `in`.okcredit.merchant.server.BusinessRemoteServer
import `in`.okcredit.merchant.server.BusinessRemoteServerImpl
import `in`.okcredit.merchant.server.internal.IdentityApiClient
import `in`.okcredit.merchant.server.internal.MerchantApiClient
import `in`.okcredit.merchant.server.internal.MerchantAuiClient
import `in`.okcredit.merchant.store.BusinessLocalSource
import `in`.okcredit.merchant.store.BusinessLocalSourceImpl
import `in`.okcredit.merchant.store.database.BusinessDao
import `in`.okcredit.merchant.store.database.BusinessDatabase
import `in`.okcredit.merchant.store.sharedprefs.IndividualPreferencesMigrationImpl
import `in`.okcredit.merchant.usecase.*
import `in`.okcredit.merchant.usecase.IsMultipleAccountEnabledImpl
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
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import java.lang.IllegalStateException

@dagger.Module
abstract class BusinessModule {

    @Binds
    @AppScope
    abstract fun api(repository: BusinessRepositoryImpl): BusinessRepository

    @Binds
    @AppScope
    abstract fun setActiveBusinessId(setActiveBusinessId: SetActiveBusinessIdImpl): SetActiveBusinessId

    @Binds
    @AppScope
    abstract fun syncBusiness(syncBusiness: SyncBusinessImpl): SyncBusiness

    @Binds
    @AppScope
    abstract fun store(store: BusinessLocalSourceImpl): BusinessLocalSource

    @Binds
    @AppScope
    abstract fun server(merchantServer: BusinessRemoteServerImpl): BusinessRemoteServer

    @Binds
    @AppScope
    abstract fun syncer(syncer: BusinessSyncerImpl): BusinessSyncer

    @Binds
    @AppScope
    abstract fun getActiveMerchant(getActiveMerchant: GetActiveBusinessImpl): GetActiveBusiness

    @Binds
    @Reusable
    abstract fun updateMerchant(updateMerchant: UpdateBusinessImpl): UpdateBusiness

    @Binds
    @AppScope
    abstract fun merchantNavigator(merchantNavigator: BusinessNavigatorImpl): BusinessNavigator

    @Binds
    @AppScope
    abstract fun isMultipleAccountEnabled(isMultipleAccountEnabled: IsMultipleAccountEnabledImpl): IsMultipleAccountEnabled

    @Binds
    @IntoMap
    @WorkerKey(BusinessSyncerImpl.SyncMerchantWorker::class)
    abstract fun workerSyncMerchantWorker(factory: BusinessSyncerImpl.SyncMerchantWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(BusinessSyncerImpl.SyncMerchantCategoriesAndBusinessTypesWorker::class)
    abstract fun workerSyncMerchantCategoriesAndBusinessTypesWorker(factory: BusinessSyncerImpl.SyncMerchantCategoriesAndBusinessTypesWorker.Factory): ChildWorkerFactory

    @Binds
    @AppScope
    abstract fun getActiveBusinessId(getActiveBusinessId: GetActiveBusinessIdImpl): GetActiveBusinessId

    @Binds
    @AppScope
    abstract fun syncBusinessData(syncBusinessData: SyncBusinessDataImpl): SyncBusinessData

    @Binds
    @AppScope
    abstract fun getBusinessIdList(getBusinessIdList: GetBusinessIdListImpl): GetBusinessIdList

    @Binds
    @AppScope
    abstract fun getBusinessIdList2(getBusinessIdList: GetBusinessIdListImpl): DefaultPreferences.GetBusinessIdListForDefaultPreferencesMigration

    @Binds
    @AppScope
    abstract fun migrationHelper(migrationHelper: MultipleAccountsDatabaseMigrationHandlerImpl): MultipleAccountsDatabaseMigrationHandler

    @Binds
    @AppScope
    abstract fun individualPreferencesMigration(userPreferencesMigration: IndividualPreferencesMigrationImpl): IndividualPreferencesMigration

    @Binds
    @AppScope
    abstract fun getBusiness(getBusiness: GetBusinessImpl): GetBusiness

    @Binds
    @AppScope
    abstract fun businessScopedPreferenceWithDefaultBusinessId(
        businessScopedPreferenceWithDefaultBusinessId: BusinessScopedPreferenceWithActiveBusinessIdImpl,
    ): BusinessScopedPreferenceWithActiveBusinessId

    companion object {

        @Provides
        fun database(context: Context): BusinessDatabase {
            return BusinessDatabase.getInstance(context)
        }

        @Provides
        @Reusable
        fun dao(database: BusinessDatabase): BusinessDao {
            return database.merchantDataBaseDao()
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
        ): MerchantApiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }

        @Provides
        @JvmStatic
        internal fun identityApiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): IdentityApiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.IDENTITY_BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }

        @Provides
        internal fun merchantApiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): MerchantAuiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.MERCHANT_BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
