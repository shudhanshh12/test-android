package tech.okcredit.android.communication.sdk

import android.os.Looper
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
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
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import tech.okcredit.android.communication.BuildConfig
import tech.okcredit.android.communication.CommunicationRemoteSource
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.CommunicationRepositoryImpl
import tech.okcredit.android.communication.brodcaste_receiver.ApplicationShareReceiver
import tech.okcredit.android.communication.brodcaste_receiver.NotificationActionBroadcastReceiver
import tech.okcredit.android.communication.brodcaste_receiver.NotificationDeleteReceiver
import tech.okcredit.android.communication.server.ApiClient
import tech.okcredit.android.communication.server.CommunicationRemoteSourceImpl
import tech.okcredit.android.communication.services.MessagingService
import tech.okcredit.android.communication.workers.CommunicationProcessNotificationWorker
import tech.okcredit.android.communication.workers.CommunicationProcessSyncNotificationWorker

@dagger.Module
abstract class CommunicationModule {

    @Binds
    @IntoMap
    @WorkerKey(CommunicationProcessNotificationWorker::class)
    abstract fun processNotificationWorker(factory: CommunicationProcessNotificationWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(CommunicationProcessSyncNotificationWorker::class)
    abstract fun processSyncNotificationWorker(factory: CommunicationProcessSyncNotificationWorker.Factory): ChildWorkerFactory

    @Binds
    @AppScope
    abstract fun repository(repository: CommunicationRepositoryImpl): CommunicationRepository

    @Binds
    @Reusable
    abstract fun remoteSource(remoteSource: CommunicationRemoteSourceImpl): CommunicationRemoteSource

    @ContributesAndroidInjector
    abstract fun messagingService(): MessagingService

    @ContributesAndroidInjector
    abstract fun notificationActionBroadcastReceiver(): NotificationActionBroadcastReceiver

    @ContributesAndroidInjector
    abstract fun notificationDeleteReceiver(): NotificationDeleteReceiver

    @ContributesAndroidInjector
    abstract fun applicationShareReceiver(): ApplicationShareReceiver

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
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): ApiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
