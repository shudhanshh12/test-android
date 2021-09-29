package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderMode
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateReminderMode @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val updateCustomer: Lazy<UpdateCustomer>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute(
        customerId: String,
        reminderMode: ReminderMode,
        responseData: BulkReminderV2Contract.ResponseData,
    ): BulkReminderV2Contract.ResponseData = withContext(Dispatchers.IO) {

        // update the reminderMode of customer profile
        updateReminderModeOnServer(customerId, reminderMode).await()

        return@withContext updateReminderModeInResponseData(customerId, responseData, reminderMode)
    }

    private fun updateReminderModeInResponseData(
        customerId: String,
        responseData: BulkReminderV2Contract.ResponseData,
        reminderMode: ReminderMode,
    ): BulkReminderV2Contract.ResponseData {
        return responseData.copy(
            reminderProfiles = responseData.reminderProfiles.map { reminderProfile ->
                if (reminderProfile.customerId == customerId) {
                    reminderProfile.copy(reminderMode = reminderMode)
                } else {
                    reminderProfile
                }
            }
        )
    }

    private fun updateReminderModeOnServer(customerId: String, reminderMode: ReminderMode): Completable {
        return getActiveBusinessId.get().execute()
            .flatMapObservable { businessId ->
                customerRepo.get().getCustomer(customerId, businessId)
            }.firstOrError()
            .flatMapCompletable { customer ->
                updateCustomer.get().execute(
                    customer.id,
                    customer.description,
                    customer.address,
                    customer.profileImage,
                    customer.mobile,
                    customer.lang,
                    reminderMode.value,
                    customer.isTxnAlertEnabled(),
                    false,
                    false,
                    customer.dueInfo_activeDate,
                    false,
                    false,
                    false,
                    false,
                    customer.state,
                    false
                )
            }
    }
}
