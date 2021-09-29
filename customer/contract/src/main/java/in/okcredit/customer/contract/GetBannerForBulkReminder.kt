package `in`.okcredit.customer.contract

import kotlinx.coroutines.flow.Flow

interface GetBannerForBulkReminder {

    fun execute(): Flow<BulkReminderModel>
}
