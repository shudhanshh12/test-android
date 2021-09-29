package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.GetConnectionStatus
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.workmanager.OkcWorkManager
import javax.inject.Inject

class UpdateLastReminderSentTime @Inject constructor(
    private val getConnectionStatus: Lazy<GetConnectionStatus>,
    private val syncLastReminderTime: Lazy<SyncLastReminderTime>,
    private val workManager: Lazy<OkcWorkManager>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute() {
        withContext(Dispatchers.IO) {
            val internalAvailable = getConnectionStatus.get().executeUnwrapped().asFlow().first()

            if (internalAvailable) {
                syncLastReminderTime.get().execute()
            } else {
                val activeBusinessId = getActiveBusinessId.get().execute().await()
                SyncLastReminderTime.schedule(workManager.get(), activeBusinessId)
            }
        }
    }
}
