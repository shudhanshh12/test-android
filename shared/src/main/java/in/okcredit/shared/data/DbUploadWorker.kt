package `in`.okcredit.shared.data

import `in`.okcredit.shared.usecase.DbFileUploader
import android.content.Context
import androidx.work.*
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseRxWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DbUploadWorker @Inject constructor(
    private val workManager: Lazy<OkcWorkManager>
) {
    companion object {
        const val WORKER_NAME = "DbUploadWorker"
        const val WORKER_TAG = "DB_UPLOAD_WORKER"
    }

    fun schedule() {
        val workName = WORKER_NAME
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(Worker::class.java)
            .addTag(WORKER_TAG)
            .addTag(workName)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                30, TimeUnit.SECONDS
            )
            .setInputData(
                Data.Builder()
                    .build()
            )
            .build()
            .enableWorkerLogging()

        workManager.get()
            .schedule(workName, Scope.Individual, ExistingWorkPolicy.REPLACE, workRequest)
    }

    class Worker constructor(
        private val context: Context,
        private val params: WorkerParameters,
        private val dbFileUploader: DbFileUploader
    ) : BaseRxWorker(context, params) {
        override fun doRxWork(): Completable {
            return uploadAllDbFiles()
        }

        private fun uploadAllDbFiles(): Completable {
            val iterator = getDbFilesIterator()
            return uploadNext(iterator)
        }

        private fun getDbFilesIterator() = context.databaseList()
            .filter { it.startsWith("okcredit") && it.endsWith(".db") }
            .iterator()

        private fun uploadNext(iterator: Iterator<String>): Completable {
            while (iterator.hasNext()) {
                return uploadDbFile(iterator.next())
                    .andThen(uploadNext(iterator))
            }
            return Completable.complete()
        }

        private fun uploadDbFile(fileName: String) =
            dbFileUploader.execute(context.getDatabasePath(fileName))
                .doOnComplete {
                    Timber.d("uploaded $fileName")
                }

        class Factory @Inject constructor(
            private val dbFileUploader: DbFileUploader
        ) : ChildWorkerFactory {
            override fun create(context: Context, params: WorkerParameters): ListenableWorker {
                return Worker(context, params, dbFileUploader)
            }
        }
    }
}
