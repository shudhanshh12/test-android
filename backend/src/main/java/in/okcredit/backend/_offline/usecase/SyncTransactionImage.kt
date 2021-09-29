package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.server.BackendRemoteSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import merchant.okcredit.accounting.model.TransactionImage
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.LogUtils.enableWorkerLogging
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkerConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncTransactionImage @Inject constructor(
    val server: Lazy<BackendRemoteSource>,
    val workManager: Lazy<OkcWorkManager>,
    val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(
        transactionImage: TransactionImage,
        businessId: String? = null,
    ): Completable {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapCompletable { _businessId ->
            server.get().createTransactionImage(transactionImage, _businessId)
        }
    }

    fun schedule(
        transactionImage: TransactionImage,
        businessId: String,
    ): Completable {
        return Completable.fromAction {
            val workCategory = "sync_transaction_image"
            val workName = "sync_transaction_image${transactionImage.url}"
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
                .addTag(workCategory)
                .addTag(workName)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .setInputData(
                    Data.Builder()
                        .putString(Worker.TRANSACTION_IMAGE_ID, transactionImage.id)
                        .putString(Worker.TRANSACTION_IMAGE_REQUEST_ID, transactionImage.request_id)
                        .putString(Worker.TRANSACTION_IMAGE_TXN_ID, transactionImage.transaction_id)
                        .putString(Worker.TRANSACTION_IMAGE_URL, transactionImage.url)
                        .putString(
                            Worker.TRANSACTION_IMAGE_CREATION_TIME,
                            transactionImage.create_time.millis.toString()
                        )
                        .putString(Worker.BUSINESS_ID, businessId)
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
        private val syncTransactionImage: Lazy<SyncTransactionImage>,
    ) : BaseRxWorker(context, params, WorkerConfig(allowUnlimitedRun = true)) {

        companion object {

            const val BUSINESS_ID = "business_id"
            const val TRANSACTION_IMAGE_CREATION_TIME = "creation_time"
            const val TRANSACTION_IMAGE_URL = "url"
            const val TRANSACTION_IMAGE_TXN_ID = "txn_id"
            const val TRANSACTION_IMAGE_REQUEST_ID = "request_id"
            const val TRANSACTION_IMAGE_ID: String = "image_id"
        }

        override fun doRxWork(): Completable {
            val creationTime = inputData.getString(TRANSACTION_IMAGE_CREATION_TIME)
                ?: return Completable.fromCallable { Result.failure() }
            val url = inputData.getString(TRANSACTION_IMAGE_URL)
                ?: return Completable.fromCallable { Result.failure() }
            val txnid = inputData.getString(TRANSACTION_IMAGE_TXN_ID)
                ?: return Completable.fromCallable { Result.failure() }
            val requestid = inputData.getString(TRANSACTION_IMAGE_REQUEST_ID)
                ?: return Completable.fromCallable { Result.failure() }
            val id = inputData.getString(TRANSACTION_IMAGE_ID)
                ?: return Completable.fromCallable { Result.failure() }
            val businessId = inputData.getString(BUSINESS_ID)
            return try {
                val txnImage = TransactionImage(
                    id,
                    requestid,
                    txnid,
                    url,
                    DateTime(creationTime.toLong())
                )
                return syncTransactionImage.get().execute(txnImage, businessId)
            } catch (e: Exception) {
                Completable.fromCallable { Result.retry() }
            }
        }

        class Factory @Inject constructor(private val syncTransactionImage: Lazy<SyncTransactionImage>) :
            ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, syncTransactionImage)
            }
        }
    }
}
