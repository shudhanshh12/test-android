package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.supplier.TestData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import merchant.okcredit.supplier.contract.IsNetworkReminderEnabled
import org.junit.Test

class PutNotificationReminderImplTest {

    private val isNetworkReminderEnabled: IsNetworkReminderEnabled = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val putNotificationReminderImpl = PutNotificationReminderImpl(
        { isNetworkReminderEnabled },
        { supplierCreditRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `when feature is enabled create notification reminder return success`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(isNetworkReminderEnabled.execute()).thenReturn(Single.just(true))
        whenever(
            supplierCreditRepository.createNotificationReminder(
                TestData.ACCOUNT_ID,
                TestData.BUSINESS_ID
            )
        ).thenReturn(Single.just(true))

        val response = putNotificationReminderImpl.execute(TestData.ACCOUNT_ID).test()

        response.assertComplete()

        verify(isNetworkReminderEnabled).execute()
        verify(supplierCreditRepository).createNotificationReminder(TestData.ACCOUNT_ID, TestData.BUSINESS_ID)

        response.dispose()
    }

    @Test
    fun `when feature is enabled create notification reminder return failure`() {

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(isNetworkReminderEnabled.execute()).thenReturn(Single.just(true))
        whenever(
            supplierCreditRepository.createNotificationReminder(
                TestData.ACCOUNT_ID,
                TestData.BUSINESS_ID
            )
        ).thenReturn(Single.just(false))

        val response = putNotificationReminderImpl.execute(TestData.ACCOUNT_ID).test()

        response.assertComplete()

        verify(isNetworkReminderEnabled).execute()
        verify(supplierCreditRepository).createNotificationReminder(TestData.ACCOUNT_ID, TestData.BUSINESS_ID)

        response.dispose()
    }

    @Test
    fun `when feature is disabled create notification reminder do nothing`() {

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(isNetworkReminderEnabled.execute()).thenReturn(Single.just(false))

        val response = putNotificationReminderImpl.execute(TestData.ACCOUNT_ID).test()

        response.assertComplete()

        verify(isNetworkReminderEnabled).execute()
        verify(supplierCreditRepository, times(0)).createNotificationReminder(TestData.ACCOUNT_ID, TestData.BUSINESS_ID)

        response.dispose()
    }
}
