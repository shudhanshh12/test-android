package tech.okcredit.android.referral.sdk

import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.referral.contract.usecase.GetShareAppIntent
import android.os.Looper
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.referral.BuildConfig
import tech.okcredit.android.referral.ReferralRepositoryImpl
import tech.okcredit.android.referral.data.ReferralApiService
import tech.okcredit.android.referral.share.usecase.GetShareAppIntentImpl
import tech.okcredit.android.referral.usecase.GetReferralLinkImpl

@dagger.Module
abstract class ReferralModule {

    @Binds
    @AppScope
    abstract fun repository(repository: ReferralRepositoryImpl): ReferralRepository

    @Binds
    @AppScope
    abstract fun getReferralLink(getReferralLink: GetReferralLinkImpl): GetReferralLink

    @Binds
    @AppScope
    abstract fun getShareAppIntent(shareAppIntent: GetShareAppIntentImpl): GetShareAppIntent

    companion object {

        @Suppress("MemberVisibilityCanBePrivate")
        internal fun checkMainThread() {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                debug {
//                    throw IllegalStateException("Initialized on main thread.")
                }
                release {
                    RecordException.recordException(IllegalStateException("Initialized on main thread."))
                }
            }
        }

        @Provides
        internal fun referralApiService(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): ReferralApiService {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.REFERRAL_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
