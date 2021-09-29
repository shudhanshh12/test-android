package tech.okcredit.base.network.di

import dagger.Module
import dagger.Provides
import dagger.Reusable
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
abstract class RetrofitModule {

    companion object {

        @Provides
        @Reusable
        fun gsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

        @Provides
        @Reusable
        fun rxJava2CallAdapterFactory(): RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()
    }
}
