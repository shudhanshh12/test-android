package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderData
import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderForUi
import `in`.okcredit.supplier.TestData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.supplier.contract.IsNetworkReminderEnabled
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils

class GetNotificationReminderForHomeTest {

    private val isNetworkReminderEnabled: IsNetworkReminderEnabled = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getNotificationReminderForHome = GetNotificationReminderForHome(
        { isNetworkReminderEnabled },
        { supplierCreditRepository },
        { getActiveBusinessId }
    )

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `execute when feature is disabled`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(isNetworkReminderEnabled.execute()).thenReturn(Single.just(false))

        val result = getNotificationReminderForHome.execute().test()

        result.assertValue(GetNotificationReminderForHome.NotificationReminderResponse(false))

        verify(isNetworkReminderEnabled).execute()
        verify(supplierCreditRepository, times(0)).getNotificationReminderData(TestData.BUSINESS_ID)

        result.dispose()
    }

    @Test
    fun `execute when feature is enabled and reminder not found`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        whenever(isNetworkReminderEnabled.execute()).thenReturn(Single.just(true))
        whenever(supplierCreditRepository.getNotificationReminderData(TestData.BUSINESS_ID)).thenReturn(Single.just(listOf<NotificationReminderData>()))

        val result = getNotificationReminderForHome.execute().test()
        result.assertValue(GetNotificationReminderForHome.NotificationReminderResponse(false))

        verify(isNetworkReminderEnabled).execute()
        verify(supplierCreditRepository, times(1)).getNotificationReminderData(TestData.BUSINESS_ID)

        result.dispose()
    }

    @Test
    fun `execute when feature is enabled and reminder data found`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))

        val notificationReminderData = NotificationReminderData(
            createdAt = "1627364420",
            name = "test merchant",
            profileImage = "https:://xyz.png",
            balance = 10000,
            lastPaymentDate = 1627364420,
            lastPayment = 120,
            accountId = "test_account_id",
            notificationId = "test_notification_id"
        )

        val notificationReminderForUi = NotificationReminderForUi(
            totalNotificationCount = 1,
            name = "test merchant",
            profileImage = "https:://xyz.png",
            balance = "100",
            lastPayment = "1.20",
            lastPaymentDate = "27 Jul 2021",
            accountId = "test_account_id",
            notificationId = "test_notification_id",
            balanceInPaisa = "10000",
            lastPaymentInPaisa = "120",
        )
        whenever(isNetworkReminderEnabled.execute()).thenReturn(Single.just(true))
        whenever(supplierCreditRepository.getNotificationReminderData(TestData.BUSINESS_ID)).thenReturn(
            Single.just(
                listOf(
                    notificationReminderData,
                    notificationReminderData
                )
            )
        )

        val result = getNotificationReminderForHome.execute().test()
        result.assertValue(GetNotificationReminderForHome.NotificationReminderResponse(true, notificationReminderForUi))
        verify(isNetworkReminderEnabled).execute()
        verify(supplierCreditRepository, times(1)).getNotificationReminderData(TestData.BUSINESS_ID)

        result.dispose()
    }
}
