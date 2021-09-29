package merchant.okcredit.accounting.di

import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import merchant.okcredit.accounting.BuildConfig
import merchant.okcredit.accounting.contract.AccountingRepository
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportData
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import merchant.okcredit.accounting.repo.AccountingRepositoryImpl
import merchant.okcredit.accounting.server.AccountingApiClient
import merchant.okcredit.accounting.usecases.GetCustomerSupportDataImpl
import merchant.okcredit.accounting.usecases.GetCustomerSupportTypeImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils

@dagger.Module
abstract class AccountingModule {

    @Binds
    @Reusable
    abstract fun getCustomerSupportTypeImpl(getCustomerSupportTypeImpl: GetCustomerSupportTypeImpl): GetCustomerSupportType

    @Binds
    @Reusable
    abstract fun accountingRepositoryImpl(accountingRepositoryImpl: AccountingRepositoryImpl): AccountingRepository

    @Binds
    @Reusable
    abstract fun getFirebaseConfigDataImpl(getCustomerSupportDataImpl: GetCustomerSupportDataImpl): GetCustomerSupportData

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): AccountingApiClient {
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
