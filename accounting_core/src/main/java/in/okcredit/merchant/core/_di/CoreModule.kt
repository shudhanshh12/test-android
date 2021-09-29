package `in`.okcredit.merchant.core._di

import `in`.okcredit.accounting_core.contract.SuggestedCustomerIdsForAddTransaction
import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
import `in`.okcredit.merchant.core.BuildConfig
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.CoreSdkImpl
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.server.CoreRemoteSourceImpl
import `in`.okcredit.merchant.core.server.internal.CoreApiClient
import `in`.okcredit.merchant.core.server.internal.EmptyStringToNullAdapter
import `in`.okcredit.merchant.core.store.CoreLocalSource
import `in`.okcredit.merchant.core.store.CoreLocalSourceImpl
import `in`.okcredit.merchant.core.store.database.CoreDatabase
import `in`.okcredit.merchant.core.store.database.CoreDatabaseDao
import `in`.okcredit.merchant.core.sync.CoreTransactionSyncer
import `in`.okcredit.merchant.core.sync.SyncCustomer
import `in`.okcredit.merchant.core.sync.SyncCustomers
import `in`.okcredit.merchant.core.sync.SyncTransactions
import `in`.okcredit.merchant.core.sync.SyncTransactionsCommands
import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@dagger.Module
abstract class CoreModule {

    @Binds
    @AppScope
    abstract fun api(api: CoreSdkImpl): CoreSdk

    @Binds
    @AppScope
    abstract fun store(coreStore: CoreLocalSourceImpl): CoreLocalSource

    @Binds
    @AppScope
    abstract fun server(coreServer: CoreRemoteSourceImpl): CoreRemoteSource

    @Binds
    @AppScope
    abstract fun syncer(coreSyncer: SyncTransactions): CoreTransactionSyncer

    @Binds
    @AppScope
    abstract fun suggestedCustomerIdsForAddTransaction(coreSyncer: CoreSdkImpl): SuggestedCustomerIdsForAddTransaction

    @Binds
    @IntoMap
    @WorkerKey(SyncTransactionsCommands.Worker::class)
    @Reusable
    abstract fun workerSyncCommands(factory: SyncTransactionsCommands.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncCustomer.Worker::class)
    @Reusable
    abstract fun workerSyncCustomer(factory: SyncCustomer.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncCustomers.Worker::class)
    @Reusable
    abstract fun workerSyncCustomers(factory: SyncCustomers.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncTransactions.Worker::class)
    @Reusable
    abstract fun workerSyncTransactions(factory: SyncTransactions.Worker.Factory): ChildWorkerFactory

    companion object {

        @Provides
        fun database(
            context: Context,
            multipleAccountsDatabaseMigrationHandler: MultipleAccountsDatabaseMigrationHandler,
        ): CoreDatabase {
            return CoreDatabase.getInstance(context, multipleAccountsDatabaseMigrationHandler)
        }

        @Provides
        @Reusable
        fun dao(database: CoreDatabase): CoreDatabaseDao {
            return database.coreDatabaseDao()
        }

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            @Core factory: MoshiConverterFactory
        ): CoreApiClient {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }

        @Provides
        @Core
        internal fun moshiConverterFactory() =
            MoshiConverterFactory.create(Moshi.Builder().add(EmptyStringToNullAdapter).build())

        @Provides
        @Core
        internal fun moshi() = Moshi.Builder().build()
    }
}
