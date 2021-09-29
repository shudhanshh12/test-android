package tech.okcredit.android.ab.sdk

import android.os.Looper
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.ab.AbRepositoryImpl
import tech.okcredit.android.ab.BuildConfig
import tech.okcredit.android.ab.server.AbApiClient
import tech.okcredit.android.ab.server.AbRemoteSource
import tech.okcredit.android.ab.server.AbRemoteSourceImpl
import tech.okcredit.android.ab.store.AbLocalSource
import tech.okcredit.android.ab.store.AbLocalSourceImpl
import tech.okcredit.android.ab.workers.ExperimentAcknowledgeWorker
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import java.lang.IllegalStateException

@dagger.Module
abstract class AbModule {

    @Binds
    @IntoMap
    @WorkerKey(ExperimentAcknowledgeWorker::class)
    @Reusable
    abstract fun experimentAcknowledgeWorker(factory: ExperimentAcknowledgeWorker.Factory): ChildWorkerFactory

    @Binds
    @AppScope
    abstract fun repository(repository: AbRepositoryImpl): AbRepository

    @Binds
    @AppScope
    abstract fun localSource(localSource: AbLocalSourceImpl): AbLocalSource

    @Binds
    @Reusable
    abstract fun remoteSource(remoteSource: AbRemoteSourceImpl): AbRemoteSource

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
            @AuthOkHttpClient authOkHttpClient: Lazy<OkHttpClient>,
        ): AbApiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(authOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
