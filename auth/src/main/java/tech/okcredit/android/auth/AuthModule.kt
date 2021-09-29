package tech.okcredit.android.auth

import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.server.AccessTokenProviderImpl
import tech.okcredit.android.auth.server.AuthApiClient
import tech.okcredit.android.auth.server.AuthInterceptor
import tech.okcredit.android.auth.server.AuthRemoteSourceImpl
import tech.okcredit.android.auth.store.AuthLocalSourceImpl
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.di.databinding.DefaultInterceptors
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.base.network.DefaultOkHttpClient

@dagger.Module
abstract class AuthModule {

    @Binds
    @Reusable
    abstract fun service(service: AuthServiceImpl): AuthService

    @Binds
    @Reusable
    abstract fun localSource(localSource: AuthLocalSourceImpl): AuthLocalSource

    @Binds
    @Reusable
    abstract fun remoteSource(remoteSource: AuthRemoteSourceImpl): AuthRemoteSource

    @Binds
    @AppScope
    abstract fun accessTokenProvider(accessTokenProvider: AccessTokenProviderImpl): AccessTokenProvider

    @Binds
    @Reusable
    abstract fun authInterceptor(authInterceptor: AuthInterceptor): Interceptor

    companion object {

        @Provides
        internal fun apiClient(
            @DefaultInterceptors defaultInterceptors: Set<@JvmSuppressWildcards Interceptor>,
            @DefaultOkHttpClient okHttpClient: OkHttpClient,
        ): AuthApiClient {

            var okHttpClientBuilder = okHttpClient
                .newBuilder()

            for (interceptor in defaultInterceptors) {
                okHttpClientBuilder = okHttpClientBuilder.addInterceptor(interceptor)
            }

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }

        @Provides
        @AuthOkHttpClient
        fun authOkHttpClient(
            @DefaultOkHttpClient defaultOkHttpClient: OkHttpClient,
            authService: AuthService,
            @DefaultInterceptors defaultInterceptors: Set<@JvmSuppressWildcards Interceptor>,
        ): OkHttpClient {
            var okHttpClientBuilder = defaultOkHttpClient
                .newBuilder()
                .addInterceptor(authService.createHttpInterceptor())

            for (interceptor in defaultInterceptors) {
                okHttpClientBuilder = okHttpClientBuilder.addInterceptor(interceptor)
            }

            return okHttpClientBuilder.build()
        }

        @Provides
        internal fun protectedApiClient(
            @AuthOkHttpClient okHttpClient: Lazy<OkHttpClient>,
        ): AuthApiClient.Protected {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create()
        }
    }
}
