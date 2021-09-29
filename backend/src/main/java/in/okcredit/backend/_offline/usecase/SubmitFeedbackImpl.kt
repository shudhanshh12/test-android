package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.backend.contract.SubmitFeedback
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
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SubmitFeedbackImpl @Inject constructor(
    private val remoteSource: Lazy<BackendRemoteSource>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SubmitFeedback {

    override fun execute(feedback: String?, rating: Int, businessId: String?): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            remoteSource.get().submitFeedback(feedback, rating, _businessId)
        }
    }

    override fun schedule(feedback: String?, rating: Int): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            Completable
                .fromAction {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val workRequest =
                        OneTimeWorkRequest.Builder(Worker::class.java)
                            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 2, TimeUnit.MINUTES)
                            .setConstraints(constraints)
                            .setInputData(
                                workDataOf(
                                    Worker.FEEDBACK to feedback,
                                    Worker.RATING to rating,
                                    Worker.BUSINESS_ID to businessId,
                                )
                            )
                            .build()
                    enableWorkerLogging(workRequest)
                    workManager.get()
                        .schedule(
                            "submit-feedback" + UUID.randomUUID().toString(),
                            Scope.Business(businessId),
                            ExistingWorkPolicy.KEEP,
                            workRequest
                        )
                }
        }
    }

    class Worker(
        context: Context,
        workerParams: WorkerParameters,
        private val submitFeedback: Lazy<SubmitFeedbackImpl>,
    ) :
        BaseRxWorker(context, workerParams, WorkerConfig(allowUnlimitedRun = false)) {

        @Inject

        override fun doRxWork(): Completable {

            if (inputData.getString(FEEDBACK) == null) {
                Completable.complete()
            }
            val feedback: String = inputData.getString(FEEDBACK)!!
            val rating: Int = inputData.getInt(RATING, 0)
            val businessId = inputData.getString(BUSINESS_ID)
            return if (rating != 0) {
                submitFeedback.get().execute(feedback, rating, businessId)
            } else {
                Completable.complete()
            }
        }

        class Factory @Inject constructor(private val submitFeedback: Lazy<SubmitFeedbackImpl>) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, submitFeedback)
            }
        }

        companion object {
            const val FEEDBACK = "feedback"
            const val RATING = "rating"
            const val BUSINESS_ID = "business_id"
        }
    }
}
