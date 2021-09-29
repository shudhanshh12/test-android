package `in`.okcredit.dynamicview.data

import `in`.okcredit.dynamicview.data.repository.DynamicViewRepositoryImpl
import android.content.Context
import androidx.work.*
import dagger.Lazy
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomizationSyncWorker constructor(
    context: Context,
    workerParameters: WorkerParameters,
    private val repository: DynamicViewRepositoryImpl,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val BUSINESS_ID = "business_id"
        fun schedule(workManager: OkcWorkManager, businessId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest =
                OneTimeWorkRequestBuilder<CustomizationSyncWorker>()
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                    .setInputData(
                        workDataOf(
                            BUSINESS_ID to businessId
                        )
                    )
                    .setConstraints(constraints)
                    .setInputData(workDataOf(BUSINESS_ID to businessId))
                    .build()
                    .enableWorkerLogging()

            workManager.schedule(
                "dynamic-view-sync",
                Scope.Business(businessId),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val businessId = inputData.getString(BUSINESS_ID)!!
            repository.syncCustomizations(businessId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    class Factory @Inject constructor(private val repository: Lazy<DynamicViewRepositoryImpl>) : ChildWorkerFactory {
        override fun create(context: Context, params: WorkerParameters) =
            CustomizationSyncWorker(context, params, repository.get())
    }
}
