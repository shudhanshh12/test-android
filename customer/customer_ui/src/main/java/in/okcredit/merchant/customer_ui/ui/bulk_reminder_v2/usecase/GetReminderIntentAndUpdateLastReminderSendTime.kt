package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import android.content.Intent
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.utils.DateTimeUtils
import javax.inject.Inject

class GetReminderIntentAndUpdateLastReminderSendTime @Inject constructor(
    private val getPaymentReminderIntent: Lazy<GetPaymentReminderIntent>,
    private val customerRepo: Lazy<CustomerRepo>,
    private val customerRepository: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute(
        reminderProfile: ReminderProfile,
    ): Intent = withContext(Dispatchers.IO) {

        val customerId = reminderProfile.customerId ?: error("Customer Id is Null")
        val activeBusinessId = getActiveBusinessId.get().execute().await()

        val paymentReminderIntent = getPaymentReminderIntent.get()
            .execute(
                customerId,
                "Bulk Reminder",
                reminderProfile.reminderMode.value,
                reminderProfile.reminderStringsObject
            ).await()

        // updating last reminder sent time in local db
        customerRepo.get().updateLastReminderSentTime(activeBusinessId, customerId, DateTimeUtils.currentDateTime())

        // saving customerId to sync last reminder sent time with server
        customerRepository.get().saveCustomerIdForSyncLastReminderSentTime(activeBusinessId, customerId)

        return@withContext paymentReminderIntent
    }
}
