package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ResponseData
import javax.inject.Inject

class AddAllSelectedReminders @Inject constructor() {
    fun execute(
        responseData: ResponseData,
        selectedReminders: List<ReminderProfile>,
    ): ResponseData {
        val updateSelectedReminders = selectedReminders.map {
            it.copy(
                isSelected = true
            )
        }
        val updatedReminderProfile = responseData.reminderProfiles.toMutableList().apply {
            removeAll(selectedReminders)
            addAll(updateSelectedReminders)
        }

        return responseData.copy(
            reminderProfiles = updatedReminderProfile
        )
    }
}
