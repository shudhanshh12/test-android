package merchant.okcredit.ok_doc.di

import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import merchant.okcredit.ok_doc.BuildConfig
import merchant.okcredit.ok_doc.OkDocRepositoryImpl
import merchant.okcredit.ok_doc.contract.OkDocRepository
import merchant.okcredit.ok_doc.server.OkDocRemoteSource
import merchant.okcredit.ok_doc.server.OkDocRemoteSourceImpl
import merchant.okcredit.ok_doc.server.OkDocService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.extensions.delegatingCallFactory

@dagger.Module
abstract class OkDocModule {

    @Binds
    @Reusable
    abstract fun okDocRepository(okDocRepository: OkDocRepositoryImpl): OkDocRepository

    @Binds
    @Reusable
    abstract fun remoteSource(okDocRemoteSourceImpl: OkDocRemoteSourceImpl): OkDocRemoteSource

    companion object {

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
            factory: GsonConverterFactory,
            callAdapterFactory: RxJava2CallAdapterFactory,
        ): OkDocService {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.OK_DOC_BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(factory)
                .addCallAdapterFactory(callAdapterFactory)
                .build()
                .create()
        }
    }
}
