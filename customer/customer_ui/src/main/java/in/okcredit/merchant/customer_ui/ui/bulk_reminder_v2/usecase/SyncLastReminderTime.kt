package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.SyncCustomers
import `in`.okcredit.customer.contract.CustomerRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.enableWorkerLogging
import tech.okcredit.android.base.workmanager.BaseCoroutineWorker
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.OkcWorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncLastReminderTime @Inject constructor(
    private val customerRepository: Lazy<CustomerRepository>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val coreSdk: Lazy<CoreSdk>,
    private val syncCustomers: Lazy<SyncCustomers>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        fun schedule(workManager: OkcWorkManager, businessId: String) {
            val constraints = Constraints.Builder().build()
            val workRequest =
                OneTimeWorkRequestBuilder<Worker>()
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
                    .enableWorkerLogging()

            workManager.schedule(
                "sync_last_reminder_time_worker",
                Scope.Business(businessId),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    suspend fun execute() {
        withContext(Dispatchers.IO) {
            syncCustomersLastReminderSendTime()
        }
    }

    private suspend fun syncCustomersLastReminderSendTime() {
        val businessId = getActiveBusinessId.get().execute().await()

        // getting dirty LastReminderSendTime customerIds from local
        val dirtyLastReminderSendTimeCustomerIds = customerRepository.get()
            .getCustomerIdForSyncLastReminderSentTime(businessId)

        if (dirtyLastReminderSendTimeCustomerIds.isNotEmpty()) {

            // getting customerIds with there LastReminder Send Time from local
            val dirtyCustomerIdsAlongWithLastReminderSendTime = customerRepo.get()
                .getDirtyLastReminderSendTime(businessId, dirtyLastReminderSendTimeCustomerIds.toList()).first()
                .filter { it.lastReminderSendTime != 0L }

            // setting those customerId with there lastReminder Send Time to server
            if (dirtyCustomerIdsAlongWithLastReminderSendTime.isNotEmpty()) {
                coreSdk.get().setCustomersLastReminderSendTimeToServer(
                    dirtyCustomerIdsAlongWithLastReminderSendTime,
                    businessId
                )
            }

            // trigger syncCustomers in case reminder send time is sent from
            // another device
            syncCustomers.get().execute().await()

            // clear the local with dirty customerIds
            customerRepository.get().clearDirtyLastReminderSendTimeCustomerIds(businessId)
        }
    }

    class Worker constructor(
        context: Context,
        workerParameters: WorkerParameters,
        private val syncLastReminderTime: Lazy<SyncLastReminderTime>,
    ) : BaseCoroutineWorker(context, workerParameters) {

        override suspend fun doActualWork() {
            syncLastReminderTime.get().execute()
        }

        class Factory @Inject constructor(
            private val syncLastReminderTime: Lazy<SyncLastReminderTime>,
        ) : ChildWorkerFactory {

            override fun create(
                context: Context,
                params: WorkerParameters,
            ) = Worker(
                context,
                params,
                syncLastReminderTime
            )
        }
    }
}
