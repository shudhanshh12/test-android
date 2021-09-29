package tech.okcredit.userSupport.sdk

import android.os.Looper
import dagger.Binds
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.di.databinding.DefaultInterceptors
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import tech.okcredit.help.sdk.BuildConfig
import tech.okcredit.userSupport.SupportLocalSource
import tech.okcredit.userSupport.SupportRemoteSource
import tech.okcredit.userSupport.SupportRepository
import tech.okcredit.userSupport.SupportRepositoryImpl
import tech.okcredit.userSupport.SupportSyncWorker
import tech.okcredit.userSupport.server.ApiClient
import tech.okcredit.userSupport.server.SupportRemoteSourceImpl
import tech.okcredit.userSupport.store.SupportLocalSourceImpl
import tech.okcredit.userSupport.usecses.SubmitFeedback

@dagger.Module
abstract class UserSupportModule {

    @Binds
    @Reusable
    abstract fun api(api: SupportRepositoryImpl): SupportRepository

    @Binds
    @Reusable
    abstract fun store(store: SupportLocalSourceImpl): SupportLocalSource

    @Binds
    @Reusable
    abstract fun server(store: SupportRemoteSourceImpl): SupportRemoteSource

    @Binds
    @IntoMap
    @WorkerKey(SupportSyncWorker::class)
    @Reusable
    abstract fun workerSupportSyncWorker(factory: SupportSyncWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SubmitFeedback.Worker::class)
    @Reusable
    abstract fun workerSubmitFeedback(factory: SubmitFeedback.Worker.Factory): ChildWorkerFactory

    companion object {

        @Suppress("MemberVisibilityCanBePrivate")
        internal fun checkMainThread() {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                debug {
                    throw IllegalStateException("Initialized on main thread.")
                }
                release {
                    RecordException.recordException(IllegalStateException("Initialized on main thread."))
                }
            }
        }

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: OkHttpClient,
            @DefaultInterceptors defaultInterceptors: Set<@JvmSuppressWildcards Interceptor>,
        ): ApiClient {
            checkMainThread()
            var okHttpClientBuilder = defaultOkHttpClient
                .newBuilder()

            for (interceptor in defaultInterceptors) {
                okHttpClientBuilder = okHttpClientBuilder.addInterceptor(interceptor)
            }

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
