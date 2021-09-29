package `in`.okcredit.storesms.di

import `in`.okcredit.storesms.BuildConfig.STORE_SMS_URL
import `in`.okcredit.storesms.StoreSmsRepository
import `in`.okcredit.storesms.StoreSmsRepositoryImpl
import `in`.okcredit.storesms.data.server.StoreSmsApiClient
import `in`.okcredit.storesms.data.worker.SyncRawSmsWorker
import dagger.Binds
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.di.databinding.DefaultInterceptors
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import tech.okcredit.base.network.DefaultOkHttpClient

@dagger.Module
abstract class StoreSmsModule {

    @Binds
    abstract fun api(impl: StoreSmsRepositoryImpl): StoreSmsRepository

    @Binds
    @IntoMap
    @WorkerKey(SyncRawSmsWorker::class)
    abstract fun syncSmsWorker(factory: SyncRawSmsWorker.Factory): ChildWorkerFactory

    companion object {

        @Provides
        @JvmStatic
        internal fun apiClient(
            @DefaultOkHttpClient defaultOkHttpClient: OkHttpClient,
            authService: AuthService,
            @DefaultInterceptors defaultInterceptors: Set<@JvmSuppressWildcards Interceptor>,
            converterFactory: GsonConverterFactory,
            callAdapterFactory: RxJava2CallAdapterFactory,
        ): StoreSmsApiClient {
            var okHttpClientBuilder = defaultOkHttpClient
                .newBuilder()
                .addInterceptor(authService.createHttpInterceptor())

            for (interceptor in defaultInterceptors) {
                okHttpClientBuilder = okHttpClientBuilder.addInterceptor(interceptor)
            }

            val okHttpClient = okHttpClientBuilder.build()
            return Retrofit.Builder()
                .baseUrl(STORE_SMS_URL)
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create()
        }
    }
}
