package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import merchant.okcredit.accounting.model.TransactionImage
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils.enableWorkerLogging
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkerConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncDeleteTransactionImage @Inject constructor(
    val remoteSource: Lazy<BackendRemoteSource>,
    val workManager: Lazy<OkcWorkManager>,
    val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(imageId: String, txnId: String, businessId: String? = null): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            remoteSource.get().deleteTransactionImage(imageId, txnId, _businessId)
        }
    }

    fun schedule(transactionImage: TransactionImage, businessId: String): Completable {
        return Completable.fromAction {
            val workCategory = "delete_transaction_image"
            val workName = "delete_transaction_image${transactionImage.url}"
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                .addTag(workCategory)
                .addTag(workName)
                .setInputData(
                    workDataOf(
                        Worker.BUSINESS_ID to businessId
                    )
                )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .setInputData(
                    Data.Builder()
                        .putString(Worker.IMAGE_ID, transactionImage.id)
                        .putString(Worker.TXN_ID, transactionImage.transaction_id)
                        .build()
                )
                .build()
            enableWorkerLogging(workRequest)
            workManager.get()
                .schedule(workName, Scope.Business(businessId), ExistingWorkPolicy.APPEND, workRequest)
        }.subscribeOn(ThreadUtils.newThread())
    }

    class Worker constructor(
        context: Context,
        params: WorkerParameters,
        private val syncDeleteTransactionImage: Lazy<SyncDeleteTransactionImage>,
    ) : BaseRxWorker(context, params, WorkerConfig(allowUnlimitedRun = true)) {

        companion object {
            const val IMAGE_ID: String = "image_id"
            const val TXN_ID: String = "txn_id"
            const val BUSINESS_ID = "business_id"
        }

        override fun doRxWork(): Completable {
            val imageId = inputData.getString(IMAGE_ID)
                ?: return Completable.fromCallable { Result.failure() }
            val txnId = inputData.getString(TXN_ID)
                ?: return Completable.fromCallable { Result.failure() }
            val businessId = inputData.getString(BUSINESS_ID)
            return syncDeleteTransactionImage.get().execute(imageId, txnId, businessId)
        }

        class Factory @Inject constructor(private val syncDeleteTransactionImage: Lazy<SyncDeleteTransactionImage>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncDeleteTransactionImage)
            }
        }
    }
}
