package `in`.okcredit.merchant.device.temp

import `in`.okcredit.merchant.device.DeviceLocalSource
import `in`.okcredit.merchant.device.DeviceRemoteSource
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeviceSyncer @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>,
) {

    fun executeSyncDevice(): Completable {
        return Completable.fromAction {
            val workName = WORKER_TAG_SYNC_EVERYTHING

            val workRequest = OneTimeWorkRequestBuilder<SyncEverythingWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(WORKER_TAG_BASE)
                .addTag(WORKER_TAG_SYNC_EVERYTHING)
                .addTag(workName)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .build()
                .enableWorkerLogging()

            workManager.get()
                .schedule(workName, Scope.Individual, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    class SyncEverythingWorker constructor(
        context: Context,
        params: WorkerParameters,
        private val deviceLocalSource: Lazy<DeviceLocalSource>,
        private val deviceRemoteSource: Lazy<DeviceRemoteSource>,
    ) : BaseRxWorker(context, params) {

        override fun doRxWork(): Completable {
            return deviceLocalSource.get().getDevice().firstOrError().doOnSuccess { Timber.d("<<<<DoWork STARTED}") }
                .flatMapCompletable {
                    return@flatMapCompletable deviceRemoteSource.get().createOrUpdateDeviceSingle(it)
                        .andThen(deviceLocalSource.get().putDevice(it.copy(syncTime = it.updateTime)))
                }.doOnError { RecordException.recordException(it) }
        }

        class Factory @Inject constructor(
            private val deviceLocalSource: Lazy<DeviceLocalSource>,
            private val deviceRemoteSource: Lazy<DeviceRemoteSource>,
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return SyncEverythingWorker(context, params, deviceLocalSource, deviceRemoteSource)
            }
        }
    }

    companion object {
        const val WORKER_TAG_BASE = "device"
        const val WORKER_TAG_SYNC_EVERYTHING = "device/syncEverything"
    }
}
