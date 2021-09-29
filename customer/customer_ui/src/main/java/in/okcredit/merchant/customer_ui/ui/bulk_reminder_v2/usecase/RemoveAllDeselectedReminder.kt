package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract
import javax.inject.Inject

class RemoveAllDeselectedReminder @Inject constructor() {

    fun execute(
        responseData: BulkReminderV2Contract.ResponseData,
        deselectedReminders: List<BulkReminderV2Contract.ReminderProfile>,
    ): BulkReminderV2Contract.ResponseData {
        val updateDeselectedReminders = deselectedReminders.map {
            it.copy(isSelected = false)
        }
        val updatedReminderProfile = responseData.reminderProfiles.toMutableList().apply {
            removeAll(deselectedReminders)
            addAll(updateDeselectedReminders)
        }

        return responseData.copy(reminderProfiles = updatedReminderProfile)
    }
}
