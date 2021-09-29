package tech.okcredit.android.base.workmanager

import android.content.Context
import androidx.work.WorkerParameters
import io.reactivex.Completable
import kotlinx.coroutines.rx2.await

@Deprecated("Please use BaseCoroutineWorker")
abstract class BaseRxWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    workerConfig: WorkerConfig = WorkerConfig(),
) : BaseCoroutineWorker(appContext, workerParams, workerConfig) {

    abstract fun doRxWork(): Completable

    override suspend fun doActualWork() {
        doRxWork().await()
    }
}
