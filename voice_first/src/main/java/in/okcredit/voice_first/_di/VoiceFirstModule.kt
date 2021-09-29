package `in`.okcredit.voice_first._di

import `in`.okcredit.voice_first.BuildConfig
import `in`.okcredit.voice_first.data.voice_collection.server.VoiceApiService
import `in`.okcredit.voice_first.ui._di.BoosterVoiceCollectionActivityModule
import `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionActivity
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.extensions.delegatingCallFactory

@Module
abstract class VoiceFirstModule {

    @ContributesAndroidInjector(modules = [BoosterVoiceCollectionActivityModule::class])
    abstract fun boosterVoiceCollectionActivity(): BoosterVoiceCollectionActivity

    companion object {

        @Provides
        @VoiceFirst
        fun retrofitVoice(
            @AuthOkHttpClient client: Lazy<OkHttpClient>,
            converterFactory: GsonConverterFactory,
        ) = Retrofit.Builder()
            .baseUrl(BuildConfig.OKPL_VOICE_URL)
            .delegatingCallFactory(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(converterFactory)
            .build()

        @Provides
        fun voiceApiService(@`in`.okcredit.voice_first._di.VoiceFirst retrofit: Retrofit) = retrofit.create(
            VoiceApiService::class.java
        )
    }
}
