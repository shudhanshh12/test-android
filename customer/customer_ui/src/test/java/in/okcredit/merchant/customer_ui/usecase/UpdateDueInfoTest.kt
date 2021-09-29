package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.usecase.UpdateCustomer
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.utils.calender.MonthView
import `in`.okcredit.merchant.customer_ui.utils.calender.OKCDate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class UpdateDueInfoTest {

    private val dueInfoRepo: DueInfoRepo = mock()
    private val updateCustomer: UpdateCustomer = mock()

    private lateinit var updateDueInfo: UpdateDueInfo

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        updateDueInfo = UpdateDueInfo(dueInfoRepo, updateCustomer)
    }

    @Test
    fun `ADDED event should call correct arguments`() {
        val activeDate = DateTime(1000L)
        val capturedDate = MonthView.CapturedDate(OKCDate(0, 1000L), MonthView.CapturedDate.DateStatus.ADDED)

        whenever(dueInfoRepo.updateCustomDueDateSet(true, TestData.CUSTOMER.id, true, activeDate)).thenReturn(Completable.complete())
        whenever(
            updateCustomer.execute(
                TestData.CUSTOMER.id,
                TestData.CUSTOMER.description,
                TestData.CUSTOMER.address,
                TestData.CUSTOMER.profileImage,
                TestData.CUSTOMER.mobile,
                TestData.CUSTOMER.lang,
                TestData.CUSTOMER.reminderMode,
                TestData.CUSTOMER.isTxnAlertEnabled(),
                false,
                false,
                activeDate,
                true,
                false,
                TestData.CUSTOMER.isAddTransactionPermissionDenied(),
                false,
                TestData.CUSTOMER.state,
                false
            )
        ).thenReturn(Completable.complete())

        val request = UpdateDueInfo.Request(capturedDate to TestData.CUSTOMER)
        val observer = updateDueInfo.execute(request).test()
        verify(dueInfoRepo).updateCustomDueDateSet(true, TestData.CUSTOMER.id, true, activeDate)
        verify(updateCustomer).execute(
            TestData.CUSTOMER.id,
            TestData.CUSTOMER.description,
            TestData.CUSTOMER.address,
            TestData.CUSTOMER.profileImage,
            TestData.CUSTOMER.mobile,
            TestData.CUSTOMER.lang,
            TestData.CUSTOMER.reminderMode,
            TestData.CUSTOMER.isTxnAlertEnabled(),
            false,
            false,
            activeDate,
            true,
            false,
            TestData.CUSTOMER.isAddTransactionPermissionDenied(),
            false,
            TestData.CUSTOMER.state,
            false
        )
        observer.assertNoErrors()
        observer.assertNoTimeout()
    }

    @Test
    fun `DELETED event should call correct arguments`() {
        val activeDate = DateTime(1000L)
        val capturedDate = MonthView.CapturedDate(OKCDate(0, 1000L), MonthView.CapturedDate.DateStatus.DELETED)

        whenever(dueInfoRepo.updateCustomDueDateSet(false, TestData.CUSTOMER.id, false, activeDate)).thenReturn(Completable.complete())
        whenever(
            updateCustomer.execute(
                TestData.CUSTOMER.id,
                TestData.CUSTOMER.description,
                TestData.CUSTOMER.address,
                TestData.CUSTOMER.profileImage,
                TestData.CUSTOMER.mobile,
                TestData.CUSTOMER.lang,
                TestData.CUSTOMER.reminderMode,
                TestData.CUSTOMER.isTxnAlertEnabled(),
                false,
                false,
                activeDate,
                false,
                true,
                TestData.CUSTOMER.isAddTransactionPermissionDenied(),
                false,
                TestData.CUSTOMER.state,
                false
            )
        ).thenReturn(Completable.complete())

        val request = UpdateDueInfo.Request(capturedDate to TestData.CUSTOMER)
        val observer = updateDueInfo.execute(request).test()
        verify(dueInfoRepo).updateCustomDueDateSet(
            isCustomDateSet = false,
            customerId = TestData.CUSTOMER.id,
            dueDateActive = false,
            dateTime = activeDate
        )
        verify(updateCustomer).execute(
            TestData.CUSTOMER.id,
            TestData.CUSTOMER.description,
            TestData.CUSTOMER.address,
            TestData.CUSTOMER.profileImage,
            TestData.CUSTOMER.mobile,
            TestData.CUSTOMER.lang,
            TestData.CUSTOMER.reminderMode,
            TestData.CUSTOMER.isTxnAlertEnabled(),
            false,
            false,
            activeDate,
            false,
            true,
            TestData.CUSTOMER.isAddTransactionPermissionDenied(),
            false,
            TestData.CUSTOMER.state,
            false
        )
        observer.assertNoErrors()
        observer.assertNoTimeout()
    }
}
