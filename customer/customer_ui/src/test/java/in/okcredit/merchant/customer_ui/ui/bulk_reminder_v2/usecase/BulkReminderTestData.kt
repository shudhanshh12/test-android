package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.common.DbReminderProfile
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ResponseData
import org.joda.time.DateTime

internal object TestData {
    val fakeResponseData = ResponseData(
        reminderProfiles = mutableListOf(
            ReminderProfile(
                reminderType = ReminderProfile.ReminderType.PENDING_REMINDER,
                customerId = "1",
                dueSince = "since 14 days",
                totalBalanceDue = "5,000",
                isSelected = true,
                lastReminderSend = "2 days",
                reminderMode = ReminderProfile.ReminderMode.WHATSAPP,
                reminderStringsObject = GetPaymentReminderIntent.ReminderStringsObject()
            ),
            ReminderProfile(
                reminderType = ReminderProfile.ReminderType.TODAYS_REMINDER,
                customerId = "2",
                dueSince = "since 14 days",
                totalBalanceDue = "5,000",
                isSelected = false,
                lastReminderSend = "2 days",
                reminderMode = ReminderProfile.ReminderMode.WHATSAPP,
                reminderStringsObject = GetPaymentReminderIntent.ReminderStringsObject()
            )
        ),
    )

    val fakeDbReminderProfile = DbReminderProfile(
        id = "13324",
        businessId = "2233",
        description = "test_1",
        profileImage = "",
        balance = 1000,
        lastPayment = DateTime.now(),
        lastReminderSendTime = null,
        reminderMode = "whatsapp",
        firstTxnTime = DateTime.now(),
        dueSinceTime = DateTime.now()
    )
}
