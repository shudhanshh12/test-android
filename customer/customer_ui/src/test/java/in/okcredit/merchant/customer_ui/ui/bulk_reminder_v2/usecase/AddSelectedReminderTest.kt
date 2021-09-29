package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import com.google.common.truth.Truth
import org.junit.Test

class AddSelectedReminderTest {
    private val addSelectedReminder = AddSelectedReminder()

    @Test
    fun `removeDeselectedReminder will set isSelected = true for current Reminder in Response Data`() {
        val fakeResponseData = TestData.fakeResponseData
        val fakeCurrentReminder = TestData.fakeResponseData.reminderProfiles.first()

        val expectedResponseData = fakeResponseData.copy(
            reminderProfiles = fakeResponseData.reminderProfiles.map { reminderProfile ->
                if (reminderProfile.customerId == fakeCurrentReminder.customerId) {
                    reminderProfile.copy(isSelected = true)
                } else {
                    reminderProfile
                }
            }
        )

        val result = addSelectedReminder.execute(
            fakeResponseData,
            fakeCurrentReminder
        )

        Truth.assertThat(result).isEqualTo(expectedResponseData)
    }
}
