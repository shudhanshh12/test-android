package `in`.okcredit.payment.di

import `in`.okcredit.payment.BuildConfig.PAYMENT_URL
import `in`.okcredit.payment.PaymentActivity
import `in`.okcredit.payment.PaymentNavigatorImpl
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.PaymentRepositoryImpl
import `in`.okcredit.payment.PspUpiActivity
import `in`.okcredit.payment.contract.PaymentNavigator
import `in`.okcredit.payment.contract.usecase.ClearPaymentEditAmountLocalData
import `in`.okcredit.payment.contract.usecase.GetPaymentAttributeFromServer
import `in`.okcredit.payment.contract.usecase.GetPaymentResult
import `in`.okcredit.payment.contract.usecase.IsPspUpiFeatureEnabled
import `in`.okcredit.payment.server.internal.PaymentApiClient
import `in`.okcredit.payment.usecases.ClearPaymentEditAmountLocalDataImpl
import `in`.okcredit.payment.usecases.GetPaymentAttributeFromServerImpl
import `in`.okcredit.payment.usecases.GetPaymentResultImpl
import `in`.okcredit.payment.usecases.IsPspUpiFeatureEnabledImpl
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory

@Module
abstract class PaymentModule {

    @Binds
    @Reusable
    abstract fun api(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    @Reusable
    abstract fun getPaymentResult(getPaymentResultImpl: GetPaymentResultImpl): GetPaymentResult

    @Binds
    @Reusable
    abstract fun clearPaymentEditAmountLocalData(clearPaymentEditAmountLocalData: ClearPaymentEditAmountLocalDataImpl): ClearPaymentEditAmountLocalData

    @Binds
    @AppScope
    abstract fun paymentNavigator(paymentNavigatorImpl: PaymentNavigatorImpl): PaymentNavigator

    @Binds
    @AppScope
    abstract fun getPaymentAttributeFromServer(getPaymentAttributeFromServer: GetPaymentAttributeFromServerImpl): GetPaymentAttributeFromServer

    @Binds
    abstract fun isPspUpiFeatureEnabled(isPspUpiFeatureEnabled: IsPspUpiFeatureEnabledImpl): IsPspUpiFeatureEnabled

    @ContributesAndroidInjector(modules = [PaymentActivityModule::class])
    abstract fun paymentActivity(): PaymentActivity

    @ContributesAndroidInjector(modules = [JuspayPspActivityModule::class])
    abstract fun juspayPspActivity(): PspUpiActivity

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            @PaymentQualifier factory: MoshiConverterFactory,
            callAdapterFactory: RxJava2CallAdapterFactory
        ): PaymentApiClient {
            return Retrofit.Builder()
                .baseUrl(PAYMENT_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create()
        }

        @Provides
        @PaymentQualifier
        internal fun moshiConverterFactory(moshi: Moshi) =
            MoshiConverterFactory.create(moshi)

        @Provides
        @PaymentQualifier
        internal fun moshi() = Moshi.Builder().build()
    }
}
