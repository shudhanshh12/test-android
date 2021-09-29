package `in`.okcredit.business_health_dashboard.di

import `in`.okcredit.business_health_dashboard.BuildConfig
import `in`.okcredit.business_health_dashboard.contract.model.usecases.BusinessHealthDashboardLocalDataOperations
import `in`.okcredit.business_health_dashboard.contract.model.usecases.GetBusinessHealthDashboardData
import `in`.okcredit.business_health_dashboard.contract.model.usecases.RefreshBusinessHealthDashboardData
import `in`.okcredit.business_health_dashboard.contract.model.usecases.SetUserPreferredTimeCadence
import `in`.okcredit.business_health_dashboard.contract.model.usecases.SubmitFeedbackForTrend
import `in`.okcredit.business_health_dashboard.datasource.remote.apiClient.BusinessHealthApiClient
import `in`.okcredit.business_health_dashboard.usecases.BusinessHealthDashboardLocalDataOperationsImpl
import `in`.okcredit.business_health_dashboard.usecases.GetBusinessHealthDashboardDataImpl
import `in`.okcredit.business_health_dashboard.usecases.RefreshBusinessHealthDashboardDataImpl
import `in`.okcredit.business_health_dashboard.usecases.SetUserPreferredTimeCadenceImpl
import `in`.okcredit.business_health_dashboard.usecases.SubmitFeedbackForTrendImpl
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

@dagger.Module
abstract class BusinessHealthDashboardModule {
    @Binds
    @Reusable
    abstract fun getBusinessHealthDashboardData(getBusinessHealthDashboardData: GetBusinessHealthDashboardDataImpl): GetBusinessHealthDashboardData

    @Binds
    @Reusable
    abstract fun setUserPreferredTimeCadence(setUserPreferredTimeCadence: SetUserPreferredTimeCadenceImpl): SetUserPreferredTimeCadence

    @Binds
    @Reusable
    abstract fun refreshBusinessHealthDashboardData(refreshBusinessHealthDashboardData: RefreshBusinessHealthDashboardDataImpl): RefreshBusinessHealthDashboardData

    @Binds
    @Reusable
    abstract fun submitFeedbackForTrend(submitFeedbackForTrendImpl: SubmitFeedbackForTrendImpl): SubmitFeedbackForTrend

    @Binds
    @Reusable
    abstract fun clearLocalDataUsecase(clearLocalData: BusinessHealthDashboardLocalDataOperationsImpl): BusinessHealthDashboardLocalDataOperations

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            @BusinessHealthDashboard converterFactory: MoshiConverterFactory,
            callAdapterFactory: RxJava2CallAdapterFactory,
        ): BusinessHealthApiClient {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create()
        }

        @Provides
        @BusinessHealthDashboard
        internal fun moshiConverterFactory() =
            MoshiConverterFactory.create(Moshi.Builder().build())
    }
}
