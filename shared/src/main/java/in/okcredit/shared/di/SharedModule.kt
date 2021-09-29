package `in`.okcredit.shared.di

import `in`.okcredit.shared.BuildConfig
import `in`.okcredit.shared.data.DbUploadWorker
import `in`.okcredit.shared.data.SharedRepo
import `in`.okcredit.shared.data.SharedRepoImpl
import `in`.okcredit.shared.data.server.OkDocsClient
import `in`.okcredit.shared.data.server.SharedRemoteSource
import `in`.okcredit.shared.data.server.SharedRemoteSourceImpl
import `in`.okcredit.shared.usecase.MigrationEventLogger
import `in`.okcredit.shared.view.CallPermissionActivity
import dagger.Binds
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@dagger.Module
abstract class SharedModule {

    @Binds
    @IntoMap
    @WorkerKey(DbUploadWorker.Worker::class)
    @Reusable
    abstract fun workerDbFileUpload(factory: DbUploadWorker.Worker.Factory): ChildWorkerFactory

    @Binds
    @AppScope
    abstract fun server(sharedRemoteSource: SharedRemoteSourceImpl): SharedRemoteSource

    @Binds
    @AppScope
    abstract fun repo(sharedRepo: SharedRepoImpl): SharedRepo

    @Binds
    @AppScope
    abstract fun sharedPreferencesMigrationEventLogger(
        logger: MigrationEventLogger,
    ): SharedPreferencesMigrationHandler.Logger

    @ContributesAndroidInjector
    abstract fun callPermissionActivity(): CallPermissionActivity

    companion object {

        @Provides
        internal fun okDocsClient(
            okHttpClient: OkHttpClient,
        ): OkDocsClient {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.OK_DOCS_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
