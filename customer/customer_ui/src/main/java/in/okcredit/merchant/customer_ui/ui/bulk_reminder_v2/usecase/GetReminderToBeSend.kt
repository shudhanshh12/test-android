package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetReminderToBeSend @Inject constructor() {

    suspend fun execute(
        currentReminderIndex: Int,
        currentReminderProfile: ReminderProfile?,
        remindersToBeDispatch: List<ReminderProfile>,
    ): Response = withContext(Dispatchers.IO) {
        if (currentReminderIndex != remindersToBeDispatch.size) {

            val currentReminderToBeSend = remindersToBeDispatch[currentReminderIndex]

            return@withContext Response(
                isCompletedSendingReminder = false,
                currentReminderProfile = currentReminderToBeSend,
                currentReminderIndex = currentReminderIndex,
            )
        } else {
            return@withContext Response(
                isCompletedSendingReminder = true,
                currentReminderProfile = currentReminderProfile ?: throw IllegalStateException("No Reminder Loaded")
            )
        }
    }

    data class Response(
        val isCompletedSendingReminder: Boolean,
        val currentReminderProfile: ReminderProfile,
        val currentReminderIndex: Int = 0,
    )
}
