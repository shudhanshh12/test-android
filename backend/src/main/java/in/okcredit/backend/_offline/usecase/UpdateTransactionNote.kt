package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils.enableWorkerLogging
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkerConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UpdateTransactionNote @Inject constructor(
    val remoteSource: BackendRemoteSource,
    val workManager: OkcWorkManager,
    val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(note: String, txnId: String, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            remoteSource.updateTransactionNote(note, txnId, _businessId)
        }
    }

    fun schedule(note: String, transactionId: String, businessId: String): Completable {
        return Completable.fromAction {
            val workCategory = "update_transaction_note"
            val workName = "update_transaction_note$transactionId $note"
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                .addTag(workCategory)
                .addTag(workName)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .setInputData(
                    workDataOf(
                        Worker.NOTE to note,
                        Worker.TXId to transactionId,
                        Worker.BUSINESS_ID to businessId
                    )
                )
                .build()
            enableWorkerLogging(workRequest)
            workManager
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.KEEP, workRequest)
        }.subscribeOn(ThreadUtils.newThread())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val updateTransactionNote: UpdateTransactionNote
    ) : BaseRxWorker(context, params, WorkerConfig(allowUnlimitedRun = true)) {

        companion object {
            const val TXId: String = "txnId"
            const val NOTE: String = "note"
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {

            val note = inputData.getString(NOTE)
                ?: Result.failure()
            val txnId = inputData.getString(TXId)
                ?: Result.failure()
            val businessId = inputData.getString(BUSINESS_ID)
            return updateTransactionNote.execute(note as String, txnId as String, businessId)
        }

        class Factory @Inject constructor(private val updateTransactionNote: UpdateTransactionNote) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, updateTransactionNote)
            }
        }
    }
}
