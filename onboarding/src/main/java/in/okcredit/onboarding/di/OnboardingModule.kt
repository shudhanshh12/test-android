package `in`.okcredit.onboarding.di

import `in`.okcredit.onboarding.BuildConfig.AUI_BASE_URL
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import `in`.okcredit.onboarding.contract.OnboardingRepo
import `in`.okcredit.onboarding.contract.marketing.AppsFlyerHelper
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.onboarding.marketing.AppsFlyerHelperImpl
import `in`.okcredit.onboarding.marketing.MarketingApiService
import `in`.okcredit.onboarding.sdk.OnboardingRepoImpl
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils

@Module
abstract class OnboardingModule {

    @Binds
    @Reusable
    abstract fun onboarding(onboarding: OnboardingRepoImpl): OnboardingRepo

    @Binds
    @AppScope
    abstract fun onboardingPreference(onboardingPreferencesImpl: OnboardingPreferencesImpl): OnboardingPreferences

    @Binds
    @Reusable
    abstract fun appsflyerHelper(appsFlyerHelperImpl: AppsFlyerHelperImpl): AppsFlyerHelper

    companion object {

        @Provides
        fun marketingApiService(
            @AuthOkHttpClient okHttpClient: Lazy<OkHttpClient>,
        ): MarketingApiService {
            return Retrofit.Builder()
                .baseUrl(AUI_BASE_URL)
                .delegatingCallFactory(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(MarketingApiService::class.java)
        }
    }
}
