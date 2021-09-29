package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.common.DbReminderProfile
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.customer.contract.GetBannerForBulkReminder
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.BulkReminderEpoxyModel.TopBanner
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ResponseData
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase.GetDefaulters.CustomersListForBulkReminder
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.Days
import tech.okcredit.android.base.utils.DateTimeUtils
import javax.inject.Inject
import kotlin.math.abs
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderMode.Companion as ReminderMode

class GetReminderProfileForCustomers @Inject constructor(
    private val getBannerForBulkReminder: Lazy<GetBannerForBulkReminder>,
) {
    suspend fun execute(
        customersList: CustomersListForBulkReminder,
        responseData: ResponseData,
    ) = withContext(Dispatchers.IO) {
        val bannerForBulkReminder = getBannerForBulkReminder.get().execute().first()

        val reminderProfiles = mutableListOf<ReminderProfile>().apply {
            addAll(
                customersList
                    .remindersWhichAreNotSendToday
                    .toReminderProfile(ReminderType.PENDING_REMINDER, responseData.reminderProfiles)
            )
            addAll(
                customersList
                    .remindersWhichAreSendToday
                    .toReminderProfile(ReminderType.TODAYS_REMINDER, responseData.reminderProfiles)
            )
        }

        ReminderProfileForBulkReminder(
            topBanner = TopBanner(
                totalBalanceDue = bannerForBulkReminder.totalBalanceDue,
                totalCustomers = bannerForBulkReminder.totalCustomers,
                defaultedSince = bannerForBulkReminder.defaulterSince
            ),
            reminderProfiles = getSortedListOfReminderProfile(reminderProfiles)
        )
    }

    private fun getSortedListOfReminderProfile(
        reminderProfiles: List<ReminderProfile>,
    ): List<ReminderProfile> {
        return reminderProfiles.toMutableList().apply {
            sortWith(
                compareByDescending<ReminderProfile> { it.lastReminderSendInDays }
                    .thenBy { it.dueSinceInDays }
                    .thenByDescending { it.totalBalanceDue }
            )
        }
    }

    data class ReminderProfileForBulkReminder(
        val reminderProfiles: List<ReminderProfile>,
        val topBanner: TopBanner? = null,
    )

    private fun List<DbReminderProfile>.toReminderProfile(
        reminderType: ReminderType,
        reminderProfiles: List<ReminderProfile>,
    ): List<ReminderProfile> = this.map { reminderProfile ->
        ReminderProfile(
            reminderType = reminderType,
            customerId = reminderProfile.id,
            customerName = reminderProfile.description,
            profileUrl = reminderProfile.profileImage ?: "",
            dueSince = getDaysAndYears(reminderProfile.dueSinceTime),
            totalBalanceDue = CurrencyUtil.formatV2(reminderProfile.balance),
            isSelected = reminderProfiles.firstOrNull { it.customerId == reminderProfile.id }?.isSelected ?: false,
            lastReminderSend = getDaysAndYears(reminderProfile.lastReminderSendTime),
            reminderMode = ReminderMode.from(reminderProfile.reminderMode),
            reminderStringsObject = getReminderStringObject(),
            lastReminderSendInDays = getDaysCount(reminderProfile.lastReminderSendTime),
            dueSinceInDays = getDaysCount(reminderProfile.dueSinceTime)
        )
    }

    private fun getReminderStringObject(): GetPaymentReminderIntent.ReminderStringsObject {
        return GetPaymentReminderIntent.ReminderStringsObject(
            paymentReminderText = R.string.payment_reminder_text,
            toMobile = R.string.to_mobile,
            dueOn = R.string.due_as_on,
        )
    }

    private fun getDaysAndYears(fromDateTime: DateTime?): String {
        return if (fromDateTime == null || fromDateTime.millis <= 0L) {
            "0"
        } else {
            val currentDate = DateTimeUtils.currentDateTime()
            val daysCount = abs(Days.daysBetween(currentDate, fromDateTime).days)
            return when {
                daysCount == 0 -> "${daysCount}h"
                daysCount < 30 -> "${daysCount}d"
                daysCount >= 365 -> "${daysCount / 365}y"
                else -> "${daysCount / 30}m"
            }
        }
    }

    private fun getDaysCount(fromDateTime: DateTime?): Int {
        return if (fromDateTime == null || fromDateTime.millis <= 0L) {
            0
        } else {
            val currentDate = DateTimeUtils.currentDateTime()
            return abs(Days.daysBetween(currentDate, fromDateTime).days)
        }
    }
}
