package `in`.okcredit.sales_sdk.sdk

import `in`.okcredit.sales_sdk.BuildConfig
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.SalesRepositoryImpl
import `in`.okcredit.sales_sdk.server.ApiClient
import `in`.okcredit.sales_sdk.server.SalesRemoteSource
import `in`.okcredit.sales_sdk.server.SalesRemoteSourceImpl
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils

@dagger.Module
abstract class SalesModule {

    @Binds
    @Reusable
    abstract fun api(api: SalesRepositoryImpl): SalesRepository

    @Binds
    @Reusable
    abstract fun server(server: SalesRemoteSourceImpl): SalesRemoteSource

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): ApiClient {
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
