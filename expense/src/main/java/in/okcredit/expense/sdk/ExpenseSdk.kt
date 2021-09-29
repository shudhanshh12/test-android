package `in`.okcredit.expense.sdk

import `in`.okcredit.expense.BuildConfig
import `in`.okcredit.expense.ExpenseRepository
import `in`.okcredit.expense.ExpenseRepositoryImpl
import `in`.okcredit.expense.server.ExpenseApiClient
import `in`.okcredit.expense.server.ExpenseRemoteSource
import `in`.okcredit.expense.server.ExpenseRemoteSourceImpl
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
abstract class ExpenseModule {

    @Binds
    @Reusable
    abstract fun repository(repository: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Reusable
    abstract fun remoteSource(remoteSource: ExpenseRemoteSourceImpl): ExpenseRemoteSource

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): ExpenseApiClient {
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
