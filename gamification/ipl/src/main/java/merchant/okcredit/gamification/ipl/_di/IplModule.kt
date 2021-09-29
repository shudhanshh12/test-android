package merchant.okcredit.gamification.ipl._di

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import merchant.okcredit.gamification.ipl.BuildConfig
import merchant.okcredit.gamification.ipl.data.IplApiService
import merchant.okcredit.gamification.ipl.game._di.GameActivityModule
import merchant.okcredit.gamification.ipl.game.ui.GameActivity
import merchant.okcredit.gamification.ipl.game.ui.youtube.YoutubeActivity
import merchant.okcredit.gamification.ipl.game.ui.youtube.di.YoutubeActivityModule
import merchant.okcredit.gamification.ipl.rewards.ClaimRewardActivity
import merchant.okcredit.gamification.ipl.rewards._di.ClaimRewardActivityModule
import merchant.okcredit.gamification.ipl.view.IplActivity
import merchant.okcredit.gamification.ipl.view._di.IplActivityModule
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tech.okcredit.android.auth.AuthOkHttpClient

@Module
abstract class IplModule {

    @ContributesAndroidInjector(modules = [IplActivityModule::class])
    abstract fun iplActivity(): IplActivity

    @ContributesAndroidInjector(modules = [GameActivityModule::class])
    abstract fun gameActivity(): GameActivity

    @ContributesAndroidInjector(modules = [ClaimRewardActivityModule::class])
    abstract fun claimRewardActivity(): ClaimRewardActivity

    @ContributesAndroidInjector(modules = [YoutubeActivityModule::class])
    abstract fun youtubeActivity(): YoutubeActivity

    companion object {

        @Provides
        @Ipl
        fun retrofit(
            @AuthOkHttpClient client: OkHttpClient,
            converterFactory: GsonConverterFactory,
        ) = Retrofit.Builder()
            .baseUrl(BuildConfig.IPL_BASE_URL)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(converterFactory)
            .build()

        @Provides
        fun iplApiService(@Ipl retrofit: Retrofit) = retrofit.create(IplApiService::class.java)
    }
}
