package `in`.okcredit.cashback.di

import `in`.okcredit.cashback.BuildConfig
import `in`.okcredit.cashback.contract.usecase.CashbackLocalDataOperations
import `in`.okcredit.cashback.contract.usecase.GetCashbackMessageDetails
import `in`.okcredit.cashback.contract.usecase.GetCashbackRewardForPayment
import `in`.okcredit.cashback.contract.usecase.IsCustomerCashbackFeatureEnabled
import `in`.okcredit.cashback.contract.usecase.IsSupplierCashbackFeatureEnabled
import `in`.okcredit.cashback.datasource.local.CashbackLocalCacheSource
import `in`.okcredit.cashback.datasource.local.CashbackLocalCacheSourceImpl
import `in`.okcredit.cashback.datasource.remote.CashbackRemoteSource
import `in`.okcredit.cashback.datasource.remote.CashbackRemoteSourceImpl
import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackApiClient
import `in`.okcredit.cashback.datasource.remoteConfig.CashbackRemoteConfigSource
import `in`.okcredit.cashback.datasource.remoteConfig.CashbackRemoteConfigSourceImpl
import `in`.okcredit.cashback.repository.CashbackRepository
import `in`.okcredit.cashback.repository.CashbackRepositoryImpl
import `in`.okcredit.cashback.usecase.CashbackLocalDataOperationsImpl
import `in`.okcredit.cashback.usecase.GetCashbackMessageDetailsImpl
import `in`.okcredit.cashback.usecase.GetCashbackRewardForPaymentImpl
import `in`.okcredit.cashback.usecase.IsCustomerCashbackFeatureEnabledImpl
import `in`.okcredit.cashback.usecase.IsSupplierCashbackFeatureEnabledImpl
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.moshi.DateTimeAdapter

@dagger.Module
abstract class CashbackModule {

    @Binds
    @Reusable
    abstract fun cashbackLocalCacheSource(cashbackLocalCacheSource: CashbackLocalCacheSourceImpl): CashbackLocalCacheSource

    @Binds
    @Reusable
    abstract fun cashbackRemoteSource(cashbackRemoteSource: CashbackRemoteSourceImpl): CashbackRemoteSource

    @Binds
    @Reusable
    abstract fun cashbackRemoteConfigSource(cashbackRemoteConfigSource: CashbackRemoteConfigSourceImpl): CashbackRemoteConfigSource

    @Binds
    @Reusable
    abstract fun cashbackRepository(cashbackRepository: CashbackRepositoryImpl): CashbackRepository

    @Binds
    @Reusable
    abstract fun clearLocalDataUsecase(clearLocalData: CashbackLocalDataOperationsImpl): CashbackLocalDataOperations

    @Binds
    @Reusable
    abstract fun getCashbackMessageUsecase(getCashbackMessage: GetCashbackMessageDetailsImpl): GetCashbackMessageDetails

    @Binds
    @Reusable
    abstract fun getCashbackRewardForPaymentUsecase(getCashbackRewardForPayment: GetCashbackRewardForPaymentImpl): GetCashbackRewardForPayment

    @Binds
    @Reusable
    abstract fun isCustomerCashbackFeatureEnabledUsecase(isCustomerCashbackFeatureEnabled: IsCustomerCashbackFeatureEnabledImpl): IsCustomerCashbackFeatureEnabled

    @Binds
    @Reusable
    abstract fun isSupplierCashbackFeatureEnabledUsecase(isSupplierCashbackFeatureEnabled: IsSupplierCashbackFeatureEnabledImpl): IsSupplierCashbackFeatureEnabled

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            @Cashback converterFactory: MoshiConverterFactory,
            callAdapterFactory: RxJava2CallAdapterFactory,
        ): CashbackApiClient {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create()
        }

        @Provides
        @Cashback
        internal fun moshiConverterFactory(@Cashback moshi: Moshi) = MoshiConverterFactory.create(moshi)

        @Provides
        @Cashback
        internal fun moshi(dateTimeAdapter: DateTimeAdapter): Moshi {
            return Moshi.Builder().add(dateTimeAdapter).build()
        }
    }
}
