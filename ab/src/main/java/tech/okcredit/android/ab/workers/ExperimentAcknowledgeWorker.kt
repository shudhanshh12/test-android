package tech.okcredit.android.ab.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.ab.AbRepositoryImpl
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import javax.inject.Inject

class ExperimentAcknowledgeWorker constructor(
    context: Context,
    params: WorkerParameters,
    private val api: AbRepositoryImpl,
) : BaseRxWorker(context, params) {

    override fun doRxWork(): Completable {
        val experimentName = inputData.getString(AbRepositoryImpl.EXPERIMENT_NAME) ?: ""
        val experimentVariant = inputData.getString(AbRepositoryImpl.EXPERIMENT_VARIANT) ?: ""
        val experimentStatus = inputData.getInt(AbRepositoryImpl.EXPERIMENT_NAME, 0)
        val acknowledgeTime = inputData.getLong(AbRepositoryImpl.EXPERIMENT_TIME, 0)
        val businessId = inputData.getString(AbRepositoryImpl.BUSINESS_ID)

        return api.acknowledgeExperiment(
            experimentName, experimentVariant, experimentStatus, acknowledgeTime, businessId
        )
    }

    class Factory @Inject constructor(
        private val ab: Lazy<AbRepositoryImpl>,
    ) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters): ListenableWorker {
            return ExperimentAcknowledgeWorker(context, params, ab.get())
        }
    }
}
