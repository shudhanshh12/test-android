package `in`.okcredit.backend._offline._di

import `in`.okcredit.backend.BuildConfig
import `in`.okcredit.backend._offline.AppLockManagerImpl
import `in`.okcredit.backend._offline._hack.ServerConfigManagerImpl
import `in`.okcredit.backend._offline.database.internal.BackendCoreDatabase
import `in`.okcredit.backend._offline.database.internal.CustomerDao
import `in`.okcredit.backend._offline.database.internal.DueInfoDao
import `in`.okcredit.backend._offline.database.internal.TransactionDao
import `in`.okcredit.backend._offline.server.internal.ApiClient
import `in`.okcredit.backend._offline.server.internal.ReportsV2ApiClient
import `in`.okcredit.backend._offline.usecase.*
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomersImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend._offline.usecase._sync_usecases.TransactionsSyncService
import `in`.okcredit.backend._offline.usecase._sync_usecases.TransactionsSyncServiceImpl
import `in`.okcredit.backend._offline.usecase.reports_v2.DownloadReport
import `in`.okcredit.backend.contract.*
import `in`.okcredit.backend.worker.HomeDataSyncWorkerImpl
import `in`.okcredit.backend.worker.HomeRefreshSyncWorkerImpl
import `in`.okcredit.backend.worker.NonActiveBusinessesDataSyncWorkerImpl
import `in`.okcredit.backend.worker.PeriodicDataSyncWorkerImpl
import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import `in`.okcredit.merchant.device.DeviceRepository
import android.content.Context
import dagger.Binds
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.di.databinding.DefaultInterceptors
import tech.okcredit.android.base.utils.GsonUtil
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import tech.okcredit.base.network.DefaultOkHttpClient

@dagger.Module
abstract class OfflineModule {

    @Binds
    @AppScope
    abstract fun transactionsSyncService(transactionsSyncServiceImpl: TransactionsSyncServiceImpl): TransactionsSyncService

    @Binds
    @AppScope
    abstract fun serverConfigManager(serverConfigManagerImpl: ServerConfigManagerImpl): ServerConfigManager

    @Binds
    @Reusable
    abstract fun signout(signout: SignoutImpl): Signout

    @Binds
    @AppScope
    abstract fun syncTransaction(syncTransactionImpl: SyncTransactionsImpl): SyncTransaction

    @Binds
    @AppScope
    abstract fun syncCustomers(syncCustomersImpl: SyncCustomersImpl): SyncCustomers

    @Binds
    @Reusable
    abstract fun checkMobileStatus(checkMobileStatus: CheckMobileStatusImpl): CheckMobileStatus

    @Binds
    @Reusable
    abstract fun getTotalTxnCount(getTotalTxnCount: GetTotalTxnCountImpl): GetTotalTxnCount

    @Binds
    @Reusable
    abstract fun appLockManager(appLockManager: AppLockManagerImpl): AppLockManager

    @Binds
    @IntoMap
    @WorkerKey(ServerActionableCheckerWorker::class)
    abstract fun serverActionableCheckerWorker(factory: ServerActionableCheckerWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SuggestedCustomersForAddTransactionShortcutSyncWorker::class)
    abstract fun suggestedCustomersForAddTransactionShortcutSyncWorker(
        factory: SuggestedCustomersForAddTransactionShortcutSyncWorker.Factory,
    ): ChildWorkerFactory

    @Binds
    @Reusable
    abstract fun submitFeedback(submitFeedback: SubmitFeedbackImpl): SubmitFeedback

    @Binds
    @Reusable
    abstract fun getMerchantPreference(getMerchantPreference: GetMerchantPreferenceImpl): GetMerchantPreference

    @Binds
    @Reusable
    abstract fun getSpecificCustomerListImpl(getSpecificCustomerListImpl: GetSpecificCustomerListImpl): GetSpecificCustomerList

    @Binds
    @Reusable
    abstract fun getCustomerImpl(getCustomerImpl: GetCustomerImpl): GetCustomer

    @Binds
    @Reusable
    abstract fun getCustomerAccountSummary(getCustomerAccountSummary: GetAccountSummary): GetCustomerAccountNetBalance

    @Binds
    @Reusable
    abstract fun getIsCustomerTransactionAllowed(
        getIsCustomerTransactionAllowed: GetIsCustomerAddTransactionRestrictedImpl
    ): GetIsCustomerAddTransactionRestricted

    @Binds
    @Reusable
    abstract fun addFlyweightCustomerTransaction(
        addFlyweightCustomerTransaction: AddFlyweightCustomerTransactionImpl
    ): AddFlyweightCustomerTransaction

    @Binds
    @Reusable
    abstract fun getActiveCustomers(
        getFlyweightActiveCustomers: GetFlyweightActiveCustomersImpl
    ): GetFlyweightActiveCustomers

    @Binds
    @IntoMap
    @WorkerKey(DownloadReport.Worker::class)
    @Reusable
    abstract fun getUrlAndDownloadReport(factory: DownloadReport.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(HomeDataSyncWorkerImpl.Worker::class)
    @Reusable
    abstract fun homeDataSyncWorker(factory: HomeDataSyncWorkerImpl.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(HomeRefreshSyncWorkerImpl.Worker::class)
    @Reusable
    abstract fun homeRefreshSyncWorker(factory: HomeRefreshSyncWorkerImpl.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(PeriodicDataSyncWorkerImpl.Worker::class)
    @Reusable
    abstract fun periodicDataSyncWorker(factory: PeriodicDataSyncWorkerImpl.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(NonActiveBusinessesDataSyncWorkerImpl.Worker::class)
    @Reusable
    abstract fun nonActiveBusinessesDataSyncWorkerImpl(
        factory: NonActiveBusinessesDataSyncWorkerImpl.Worker.Factory
    ): ChildWorkerFactory

    companion object {

        @Provides
        @AppScope
        fun coreDatabase(
            context: Context,
            multipleAccountsDatabaseMigrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): BackendCoreDatabase {
            return BackendCoreDatabase.getInstance(context, multipleAccountsDatabaseMigrationHandler)
        }

        @Provides
        @Reusable
        fun customerDao(database: BackendCoreDatabase): CustomerDao {
            return database.customerDao()
        }

        @Provides
        @Reusable
        fun transactionDao(database: BackendCoreDatabase): TransactionDao {
            return database.transactionDao()
        }

        // WARNING: Dagger multi-bindings. Don't remove this.
        @Provides
        @IntoSet
        @AppScope
        @DefaultInterceptors
        fun deviceInterceptor(deviceRepository: DeviceRepository): Interceptor {
            return deviceRepository.interceptor
        }

        @Provides
        @Reusable
        fun dueInfoDao(database: BackendCoreDatabase): DueInfoDao {
            return database.dueInfoDao()
        }

        @Provides
        @AppScope
        fun apiClient(
            @DefaultOkHttpClient defaultOkHttpClient: OkHttpClient,
            deviceRepository: DeviceRepository,
            authService: AuthService,
        ): ApiClient {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val okHttpClientBuilder: OkHttpClient.Builder = defaultOkHttpClient
                .newBuilder()
                .addInterceptor(deviceRepository.interceptor)
                .addInterceptor(authService.createHttpInterceptor())
                .addInterceptor(interceptor)
            val okHttpClient = okHttpClientBuilder.build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtil.getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            return retrofit.create(ApiClient::class.java)
        }

        @Provides
        @AppScope
        fun reportsV2OkHttpClient(
            @DefaultOkHttpClient defaultOkHttpClient: OkHttpClient,
            deviceRepository: DeviceRepository,
            authService: AuthService,
        ): OkHttpClient {
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val okHttpClientBuilder: OkHttpClient.Builder = defaultOkHttpClient
                .newBuilder()
                .addInterceptor(deviceRepository.interceptor)
                .addInterceptor(authService.createHttpInterceptor())
                .addInterceptor(interceptor)
            return okHttpClientBuilder.build()
        }

        @Provides
        @AppScope
        fun reportsV2ApiClient(
            okHttpClient: OkHttpClient,
        ): ReportsV2ApiClient {
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.REPORTS_V2_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtil.getGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
            return retrofit.create(ReportsV2ApiClient::class.java)
        }
    }
}
