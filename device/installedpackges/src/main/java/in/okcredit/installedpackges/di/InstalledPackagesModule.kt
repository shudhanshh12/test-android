package `in`.okcredit.installedpackges.di

import `in`.okcredit.installedpackges.BuildConfig.INSTALLED_PKG_URL
import `in`.okcredit.installedpackges.InstalledPackagesRepository
import `in`.okcredit.installedpackges.InstalledPackagesRepositoryImpl
import `in`.okcredit.installedpackges.server.InstalledPackagesApiClient
import `in`.okcredit.installedpackges.worker.SyncInstalledPackagesWorker
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

@dagger.Module
abstract class InstalledPackagesModule {
    @Binds
    @Reusable
    abstract fun api(impl: InstalledPackagesRepositoryImpl): InstalledPackagesRepository

    @Binds
    @IntoMap
    @WorkerKey(SyncInstalledPackagesWorker::class)
    @Reusable
    abstract fun syncInstalledWorker(factory: SyncInstalledPackagesWorker.Factory): ChildWorkerFactory

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            @InstalledPackages factory: MoshiConverterFactory,
            callAdapterFactory: RxJava2CallAdapterFactory
        ): InstalledPackagesApiClient {
            return Retrofit.Builder()
                .baseUrl(INSTALLED_PKG_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create()
        }

        @Provides
        @InstalledPackages
        internal fun moshiConverterFactory() =
            MoshiConverterFactory.create(Moshi.Builder().build())

        @Provides
        @InstalledPackages
        internal fun moshi() = Moshi.Builder().build()
    }
}
