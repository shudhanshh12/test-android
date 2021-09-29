package tech.okcredit.android.ab.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import javax.inject.Inject

class AbSyncWorker constructor(
    context: Context,
    params: WorkerParameters,
    private val api: Lazy<AbRepository>
) : BaseRxWorker(context, params) {
    companion object {
        const val BUSINESS_ID = "business_id"
        const val SOURCE = "source"
    }

    override fun doRxWork(): Completable {
        val businessId = inputData.getString(BUSINESS_ID)
        val source = inputData.getString(SOURCE) ?: ""

        return api.get().sync(businessId, source)
    }

    class Factory @Inject constructor(private val api: Lazy<AbRepository>) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return AbSyncWorker(context, params, api)
        }
    }
}
