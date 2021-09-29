package tech.okcredit.sdk.di

import `in`.okcredit.merchant.contract.MultipleAccountsDatabaseMigrationHandler
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
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import tech.okcredit.bill_management_sdk.BuildConfig
import tech.okcredit.bills.BillRepository
import tech.okcredit.sdk.BillRepositoryImpl
import tech.okcredit.sdk.server.BillApiClient
import tech.okcredit.sdk.server.BillRemoteSource
import tech.okcredit.sdk.server.BillRemoteSourceImpl
import tech.okcredit.sdk.store.BillLocalSource
import tech.okcredit.sdk.store.BillLocalSourceImpl
import tech.okcredit.sdk.store.database.BillDatabase
import tech.okcredit.sdk.store.database.BillDatabaseDao
import tech.okcredit.sdk.workers.BillSyncWorker

@dagger.Module
abstract class BillModule {
    @Binds
    @Reusable
    abstract fun api(api: BillRepositoryImpl): BillRepository

    @Binds
    @Reusable
    abstract fun store(coreStore: BillLocalSourceImpl): BillLocalSource

    @Binds
    @Reusable
    abstract fun server(coreServer: BillRemoteSourceImpl): BillRemoteSource

    @Binds
    @IntoMap
    @WorkerKey(BillSyncWorker::class)
    @Reusable
    abstract fun experimentAcknowledgeWorker(factory: BillSyncWorker.Factory): ChildWorkerFactory

    companion object {

        @Provides
        fun database(context: Context, migrationHandler: MultipleAccountsDatabaseMigrationHandler): BillDatabase {
            return BillDatabase.getInstance(context, migrationHandler)
        }

        @Provides
        @Reusable
        fun dao(database: BillDatabase): BillDatabaseDao {
            return database.coreDatabaseDao()
        }

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            @QBill factory: MoshiConverterFactory
        ): BillApiClient {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }

        @Provides
        @QBill
        internal fun moshiConverterFactory() = MoshiConverterFactory.create()

        @Provides
        @QBill
        internal fun moshi() = Moshi.Builder().build()
    }
}
