package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.device.DeviceRepository
import android.content.Context
import androidx.work.*
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class LinkDevice @Inject constructor(
    private val deviceRepository: Lazy<DeviceRepository>,
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val workManager: Lazy<OkcWorkManager>
) {

    fun execute(): Completable {
        return remoteSource.get().linkDevice(deviceRepository.get().deviceDeprecated.id)
    }

    fun schedule(): Completable {
        return Completable
            .fromAction {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val workRequest =
                    OneTimeWorkRequest.Builder(Worker::class.java)
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                enableWorkerLogging(workRequest)
                workManager.get()
                    .schedule("link-device", Scope.Individual, ExistingWorkPolicy.REPLACE, workRequest)
            }
    }

    class Worker(context: Context, workerParams: WorkerParameters, private var linkDevice: Lazy<LinkDevice>) :
        BaseRxWorker(context, workerParams) {

        override fun doRxWork(): Completable {
            return linkDevice.get().execute()
        }

        class Factory @Inject constructor(
            private var linkDevice: Lazy<LinkDevice>
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, linkDevice)
            }
        }
    }
}
