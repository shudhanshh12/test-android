package `in`.okcredit.merchant.suppliercredit.sdk

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import `in`.okcredit.merchant.suppliercredit.BuildConfig
import `in`.okcredit.merchant.suppliercredit.ISyncer
import `in`.okcredit.merchant.suppliercredit.SupplierLocalSource
import `in`.okcredit.merchant.suppliercredit.SupplierRemoteSource
import `in`.okcredit.merchant.suppliercredit.SyncerImpl
import `in`.okcredit.merchant.suppliercredit.server.ServerImpl
import `in`.okcredit.merchant.suppliercredit.server.internal.ApiClient
import `in`.okcredit.merchant.suppliercredit.store.StoreImpl
import `in`.okcredit.merchant.suppliercredit.store.database.SupplierDataBase
import `in`.okcredit.merchant.suppliercredit.store.database.SupplierDataBaseDao
import `in`.okcredit.merchant.suppliercredit.use_case.AddFlyweightSupplierTransactionImpl
import `in`.okcredit.merchant.suppliercredit.use_case.GetFlyweightActiveSuppliersImpl
import `in`.okcredit.merchant.suppliercredit.use_case.GetIsSupplierAddTransactionRestrictedImpl
import android.content.Context
import android.os.Looper
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import merchant.okcredit.suppliercredit.contract.AddFlyweightSupplierTransaction
import merchant.okcredit.suppliercredit.contract.GetFlyweightActiveSuppliers
import merchant.okcredit.suppliercredit.contract.GetIsSupplierAddTransactionRestricted
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
import java.lang.IllegalStateException

@dagger.Module
abstract class SupplierCreditModule {

    @Binds
    @AppScope
    abstract fun store(store: StoreImpl): SupplierLocalSource

    @Binds
    @AppScope
    abstract fun server(store: ServerImpl): SupplierRemoteSource

    @Binds
    @AppScope
    abstract fun syncer(syncer: SyncerImpl): ISyncer

    @Binds
    @Reusable
    abstract fun addFlyweightSupplierTransaction(
        addFlyweightSupplierTransaction: AddFlyweightSupplierTransactionImpl
    ): AddFlyweightSupplierTransaction

    @Binds
    @Reusable
    abstract fun getFlyweightActiveSuppliers(
        getFlyweightActiveSuppliers: GetFlyweightActiveSuppliersImpl
    ): GetFlyweightActiveSuppliers

    @Binds
    @Reusable
    abstract fun getIsSupplierTransactionAllowed(
        getIsSupplierTransactionAllowed: GetIsSupplierAddTransactionRestrictedImpl
    ): GetIsSupplierAddTransactionRestricted

    @Binds
    @IntoMap
    @WorkerKey(SyncerImpl.SyncEverythingWorker::class)
    abstract fun workerSyncEverythingWorker(factory: SyncerImpl.SyncEverythingWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncerImpl.SyncSupplierWorker::class)
    abstract fun workerSyncSupplierWorker(factory: SyncerImpl.SyncSupplierWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncerImpl.SyncTransactionWorker::class)
    abstract fun workerSyncTransactionWorker(factory: SyncerImpl.SyncTransactionWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncerImpl.SyncSupplierEnabledCustomerIdsWorker::class)
    abstract fun workerSyncSupplierEnabledCustomerIdsWorker(factory: SyncerImpl.SyncSupplierEnabledCustomerIdsWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncerImpl.SyncNotificationReminderWorker::class)
    abstract fun notificationReminderSyncWorker(factory: SyncerImpl.SyncNotificationReminderWorker.Factory): ChildWorkerFactory

    companion object {

        @Provides
        fun database(context: Context, migrationHandler: MultipleAccountsDatabaseMigrationHandler): SupplierDataBase {
            return SupplierDataBase.getInstance(context, migrationHandler)
        }

        @Provides
        @Reusable
        fun dao(database: SupplierDataBase): SupplierDataBaseDao {
            return database.supplierDataBaseDao()
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
        ): ApiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
