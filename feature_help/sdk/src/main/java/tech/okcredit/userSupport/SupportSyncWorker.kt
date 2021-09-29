package tech.okcredit.userSupport

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.reactivex.Completable
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import javax.inject.Inject

class SupportSyncWorker constructor(
    context: Context,
    params: WorkerParameters,
    private val api: SupportRepository
) : BaseRxWorker(context, params) {

    override fun doRxWork(): Completable {
        val businessId = inputData.getString("businessId")
        val language = inputData.getString("language")
        return api.executeSyncEverything(language!!, businessId!!)
    }

    class Factory @Inject constructor(private val api: SupportRepository) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return SupportSyncWorker(context, params, api)
        }
    }
}
