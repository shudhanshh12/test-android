package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ResponseData
import javax.inject.Inject

class RemoveDeselectedReminder @Inject constructor() {
    fun execute(
        responseData: ResponseData,
        deselectedReminderProfile: ReminderProfile,
    ): ResponseData {
        return getUpdatedResponseData(responseData, deselectedReminderProfile)
    }

    private fun getUpdatedResponseData(
        responseData: ResponseData,
        deselectedReminderProfile: ReminderProfile,
    ): ResponseData {
        return responseData.copy(
            reminderProfiles = responseData.reminderProfiles.map { reminderProfile ->
                if (reminderProfile.customerId == deselectedReminderProfile.customerId) {
                    reminderProfile.copy(isSelected = false)
                } else {
                    reminderProfile
                }
            }
        )
    }
}
