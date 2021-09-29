package `in`.okcredit.merchant.device.sdk

import `in`.okcredit.merchant.device.BuildConfig
import `in`.okcredit.merchant.device.DeviceLocalSource
import `in`.okcredit.merchant.device.DeviceRemoteSource
import `in`.okcredit.merchant.device.DeviceRepository
import `in`.okcredit.merchant.device.DeviceRepositoryImpl
import `in`.okcredit.merchant.device.server.DeviceApiClient
import `in`.okcredit.merchant.device.server.DeviceRemoteSourceImpl
import `in`.okcredit.merchant.device.store.DeviceLocalSourceImpl
import `in`.okcredit.merchant.device.temp.DeviceSyncer
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
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import tech.okcredit.base.network.DefaultOkHttpClient
import java.lang.IllegalStateException

@dagger.Module
abstract class DeviceModule {

    @Binds
    @AppScope
    abstract fun api(api: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @AppScope
    abstract fun store(store: DeviceLocalSourceImpl): DeviceLocalSource

    @Binds
    @AppScope
    abstract fun server(server: DeviceRemoteSourceImpl): DeviceRemoteSource

    @Binds
    @IntoMap
    @WorkerKey(DeviceSyncer.SyncEverythingWorker::class)
    @Reusable
    abstract fun workerCustomerTxnAlertDialogDismissWorker(factory: DeviceSyncer.SyncEverythingWorker.Factory): ChildWorkerFactory

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
            @DefaultOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): DeviceApiClient {
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
