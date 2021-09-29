package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ResponseData
import javax.inject.Inject

class AddSelectedReminder @Inject constructor() {
    fun execute(
        responseData: ResponseData,
        selectedReminderProfile: ReminderProfile,
    ): ResponseData {
        return getUpdatedResponseData(responseData, selectedReminderProfile)
    }

    private fun getUpdatedResponseData(
        responseData: ResponseData,
        selectedReminderProfile: ReminderProfile,
    ): ResponseData {
        return responseData.copy(
            reminderProfiles = responseData.reminderProfiles.map { reminderProfile ->
                if (reminderProfile.customerId == selectedReminderProfile.customerId) {
                    reminderProfile.copy(isSelected = true)
                } else {
                    reminderProfile
                }
            }
        )
    }
}
