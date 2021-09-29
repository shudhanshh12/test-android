package tech.okcredit.userSupport.usecses

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkerConfig
import tech.okcredit.userSupport.SupportRemoteSource
import tech.okcredit.userSupport.usecses.SubmitFeedback.Worker.Companion.WORKER_NAME
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SubmitFeedback @Inject constructor(
    private val remoteSource: Lazy<SupportRemoteSource>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(feedback: String, issue_type: String, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                remoteSource.get().submitFeedback(feedback, issue_type, _businessId)
            }
    }

    fun schedule(feedback: String, feedback_type: String, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId)
            .flatMapCompletable { _businessId ->
                scheduleFeedback(feedback, feedback_type, _businessId)
            }
    }

    private fun scheduleFeedback(feedback: String, feedback_type: String, businessId: String): Completable {
        return Completable
            .fromAction {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val data = Data.Builder()
                    .putString(Worker.FEEDBACK, feedback)
                    .putString(Worker.FEEDBACK_TYPE, feedback_type)
                    .putString(Worker.BUSINESS_ID, businessId)
                    .build()
                val workRequest =
                    OneTimeWorkRequest.Builder(Worker::class.java)
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 2, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .setInputData(data)
                        .build()
                enableWorkerLogging(workRequest)
                workManager.get()
                    .schedule(
                        WORKER_NAME + UUID.randomUUID().toString(),
                        Scope.Business(businessId),
                        ExistingWorkPolicy.KEEP,
                        workRequest
                    )
            }
    }

    class Worker(context: Context, workerParams: WorkerParameters, private val submitFeedback: Lazy<SubmitFeedback>) :
        BaseRxWorker(context, workerParams, WorkerConfig(allowUnlimitedRun = false)) {

        override fun doRxWork(): Completable {

            if (inputData.getString(FEEDBACK) == null) {
                Completable.complete()
            }
            val feedback: String = inputData.getString(FEEDBACK)!!
            val feedbackType: String = inputData.getString(FEEDBACK_TYPE)!!
            val businessId = inputData.getString(BUSINESS_ID)
            return submitFeedback.get().execute(feedback, feedbackType, businessId)
        }

        class Factory @Inject constructor(private val submitFeedback: Lazy<SubmitFeedback>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, submitFeedback)
            }
        }

        companion object {
            const val BUSINESS_ID = "business_id"
            const val FEEDBACK = "feedback"
            const val FEEDBACK_TYPE = "merchantId"

            const val WORKER_NAME = "submit-feedback_user-success"
        }
    }
}
