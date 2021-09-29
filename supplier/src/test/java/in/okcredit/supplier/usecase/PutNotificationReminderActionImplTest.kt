package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.supplier.TestData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class PutNotificationReminderActionImplTest {
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val putNotificationReminderImpl = PutNotificationReminderActionImpl(
        { supplierCreditRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `update notification reminder with action return success`() {
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            supplierCreditRepository.updateNotificationReminderById(
                TestData.NOTIFICATION_ID,
                TestData.STATUS
            )
        ).thenReturn(
            Completable.complete()
        )

        whenever(supplierCreditRepository.syncNotificationReminder(businessId)).thenReturn(Completable.complete())

        val response = putNotificationReminderImpl.execute(TestData.NOTIFICATION_ID, TestData.STATUS).test()
        response.assertComplete()

        verify(supplierCreditRepository).updateNotificationReminderById(TestData.NOTIFICATION_ID, TestData.STATUS)
        verify(supplierCreditRepository).syncNotificationReminder(businessId)

        response.dispose()
    }
}
