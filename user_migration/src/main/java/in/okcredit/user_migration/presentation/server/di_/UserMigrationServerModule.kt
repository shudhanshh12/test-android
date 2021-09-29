package `in`.okcredit.user_migration.presentation.server.di_

import `in`.okcredit.user_migration.BuildConfig
import `in`.okcredit.user_migration.presentation.server.UserMigrationApiClient
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.di.databinding.DefaultInterceptors
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.base.network.LongOkHttpClient

@dagger.Module
abstract class UserMigrationServerModule {

    companion object {

        @Provides
        internal fun apiClient(
            authService: AuthService,
            @LongOkHttpClient longOkHttpClient: OkHttpClient,
            @DefaultInterceptors defaultInterceptors: Set<@JvmSuppressWildcards Interceptor>
        ): UserMigrationApiClient {

            var okHttpClientBuilder = longOkHttpClient
                .newBuilder()
                .addInterceptor(authService.createHttpInterceptor())

            for (interceptor in defaultInterceptors) {
                okHttpClientBuilder = okHttpClientBuilder.addInterceptor(interceptor)
            }

            val okHttpClient = okHttpClientBuilder.build()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
