package `in`.okcredit.upgrade

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseCoroutineWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.android.base.workmanager.WorkManagerPrefs
import tech.okcredit.bills.BillRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppUpgradeWorker constructor(
    context: Context,
    workerParameters: WorkerParameters,
    private val workManagerPrefs: Lazy<WorkManagerPrefs>,
    private val billRepository: Lazy<BillRepository>,
) : BaseCoroutineWorker(context, workerParameters) {

    companion object {

        fun schedule(workManager: OkcWorkManager) {
            val constraints = Constraints.Builder().build()
            val workRequest =
                OneTimeWorkRequestBuilder<AppUpgradeWorker>()
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
                    .enableWorkerLogging()

            workManager.schedule(
                "app-upgrade-worker",
                Scope.Individual,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    override suspend fun doActualWork() {
        invalidateRateLimits()
        setBillAdoptionTime()
    }

    private suspend fun invalidateRateLimits() {
        withContext(Dispatchers.IO) {
            workManagerPrefs.get().clear()
        }
    }

    private suspend fun setBillAdoptionTime() {
        withContext(Dispatchers.IO) {
            billRepository.get().setBillAdoptionTime().await()
        }
    }

    class Factory @Inject constructor(
        private val workManagerPrefs: Lazy<WorkManagerPrefs>,
        private val billRepository: Lazy<BillRepository>,
    ) : ChildWorkerFactory {

        override fun create(
            context: Context,
            params: WorkerParameters,
        ) = AppUpgradeWorker(context, params, workManagerPrefs, billRepository)
    }
}
