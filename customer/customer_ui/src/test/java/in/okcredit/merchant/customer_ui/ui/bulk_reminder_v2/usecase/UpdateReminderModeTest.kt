package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase.TestData.fakeResponseData
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UpdateReminderModeTest {
    private val mockCustomerRepo: CustomerRepo = mock()
    private val mockUpdateCustomer: UpdateCustomer = mock()
    private val mockGetActiveBusinessId: GetActiveBusinessId = mock()

    private val updateReminderMode = UpdateReminderMode(
        { mockCustomerRepo },
        { mockUpdateCustomer },
        { mockGetActiveBusinessId }
    )

    @Test
    fun `UpdateReminderMode Should Return the updated ResponseData`() {
        runBlocking {
            // given
            val argumentCaptorString = argumentCaptor<String>()
            val fakeReminderMode = BulkReminderV2Contract.ReminderProfile.ReminderMode.SMS
            val fakeCustomerId = fakeResponseData.reminderProfiles[0].customerId ?: ""
            val fakeResponseData = fakeResponseData
            val fakeBusinessId = "12233"
            val customer = TestData.CUSTOMER.copy(id = fakeCustomerId)

            // when
            whenever(mockGetActiveBusinessId.execute()).thenReturn(Single.just(fakeBusinessId))
            whenever(
                mockCustomerRepo.getCustomer(
                    argumentCaptorString.capture(),
                    argumentCaptorString.capture()
                )
            ).thenReturn(Observable.just(customer))
            whenever(
                mockUpdateCustomer.execute(
                    customer.id,
                    customer.description,
                    customer.address,
                    customer.profileImage,
                    customer.mobile,
                    customer.lang,
                    fakeReminderMode.value,
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
            ).thenReturn(Completable.complete())

            // then
            val result = updateReminderMode.execute(
                customer.id,
                fakeReminderMode,
                fakeResponseData
            )

            Truth.assertThat(
                result.reminderProfiles.find { it.customerId == customer.id }?.reminderMode == fakeReminderMode

            ).isTrue()
        }
    }
}
