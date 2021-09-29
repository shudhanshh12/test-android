package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase

import `in`.okcredit.backend._offline.common.DbReminderProfile
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.common.truth.Truth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GetDefaultersTest {
    private val mockCustomerRepo: CustomerRepo = mock()
    private val mockFirebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getDefaulter = GetDefaulters(
        { mockCustomerRepo },
        { mockFirebaseRemoteConfig },
        { getActiveBusinessId }
    )

    @Test
    fun `usecase will return the list of pending reminder and sent todays reminder`() {
        runBlocking {
            val expectedResponse = GetDefaulters.CustomersListForBulkReminder(
                remindersWhichAreNotSendToday = listOf<DbReminderProfile>(mock(), mock(), mock()),
                remindersWhichAreSendToday = listOf(mock(), mock(), mock())
            )
            val expectedDefaulterSince = "4"
            val expectedQueryDefaulterSince = "-$expectedDefaulterSince day"
            val argumentCaptor = argumentCaptor<String>()
            val fakeBusinessId = "1345e"

            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(fakeBusinessId))

            whenever(
                mockFirebaseRemoteConfig.getString(
                    argumentCaptor.capture()
                )
            ).thenReturn(expectedDefaulterSince)

            whenever(
                mockCustomerRepo.getDefaultersForPendingReminders(
                    argumentCaptor.capture(),
                    argumentCaptor.capture()
                )
            ).thenReturn(
                flowOf(expectedResponse.remindersWhichAreNotSendToday)
            )
            whenever(
                mockCustomerRepo.getDefaultersForTodaysReminders(
                    argumentCaptor.capture(),
                    argumentCaptor.capture()
                )
            ).thenReturn(
                flowOf(expectedResponse.remindersWhichAreSendToday)
            )

            val result = getDefaulter.execute().first()

            Truth.assertThat(result).isEqualTo(expectedResponse)
            Truth.assertThat(argumentCaptor.firstValue == expectedQueryDefaulterSince)
            Truth.assertThat(argumentCaptor.secondValue == expectedQueryDefaulterSince)
            Truth.assertThat(argumentCaptor.thirdValue == "bulk_reminder_v2_defaulted_since")
        }
    }
}
