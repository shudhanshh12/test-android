package tech.okcredit.home.ui.reminder.bulk

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.base.BaseViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.base.network.NetworkError
import tech.okcredit.home.TestData
import tech.okcredit.home.TestViewModel
import tech.okcredit.home.ui.reminder.bulk.BulkReminderContract.*
import tech.okcredit.home.ui.reminder.usecase.GetBulkReminderCustomers
import tech.okcredit.home.ui.reminder.usecase.SendBulkReminder

class BulkReminderViewModelTest : TestViewModel<State, PartialState, ViewEvent>() {

    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getBulkReminderCustomers: GetBulkReminderCustomers = mock()
    private val sendBulkReminder: SendBulkReminder = mock()
    private val collectionRepository: CollectionRepository = mock()

    override fun createViewModel(): BaseViewModel<State, PartialState, ViewEvent> = BulkReminderViewModel(
        state = State(),
        getActiveBusinessId = { getActiveBusinessId },
        getBulkReminderCustomers = { getBulkReminderCustomers },
        sendBulkReminder = { sendBulkReminder },
        collectionRepository = { collectionRepository },
    )

    override fun initDependencies() {
        super.initDependencies()
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.MERCHANT.id))
    }

    @Test
    fun `load intent should get required data`() {
        whenever(getBulkReminderCustomers.execute()).thenReturn(
            Observable.just(
                listOf(TestData.CUSTOMER, TestData.CUSTOMER_2)
            )
        )
        whenever(collectionRepository.isCollectionActivated()).thenReturn(
            Observable.just(true)
        )

        pushIntent(Intent.Load)

        verify(getActiveBusinessId).execute()
        verify(collectionRepository).isCollectionActivated()
        verify(getBulkReminderCustomers).execute()

        assertLastValue { it.collectionAdopted }
        assertLastValue { it.merchantId == TestData.MERCHANT.id }
        assertLastValue {
            println(it.bulkReminderList)
            it.bulkReminderList == listOf(
                TestData.CUSTOMER.toBulkReminderItem(true),
                TestData.CUSTOMER_2.toBulkReminderItem(true)
            )
        }
    }

    @Test
    fun `select customer should update checked in customer`() {
        whenever(getBulkReminderCustomers.execute()).thenReturn(
            Observable.just(
                listOf(TestData.CUSTOMER, TestData.CUSTOMER_2)
            )
        )
        whenever(collectionRepository.isCollectionActivated()).thenReturn(
            Observable.just(true)
        )

        pushIntent(Intent.Load)
        pushIntent(Intent.SelectCustomer(TestData.CUSTOMER.id))

        assertLastValue { state ->
            state.bulkReminderList.firstOrNull { it.customerId == TestData.CUSTOMER.id }?.checked == false
        }
    }

    @Test
    fun `send reminder calls use case and sends success view event`() {
        whenever(sendBulkReminder.execute(listOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id))).thenReturn(
            Completable.complete()
        )

        pushIntent(Intent.SubmitClicked(listOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id)))

        assertLastViewEvent(ViewEvent.Success)
    }

    @Test
    fun `send reminder calls use case and sends internet error view event`() {
        whenever(sendBulkReminder.execute(listOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id))).thenReturn(
            Completable.error(NetworkError("network_error", Throwable("network error")))
        )

        pushIntent(Intent.SubmitClicked(listOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id)))

        assert(lastViewEvent() is ViewEvent.ShowError)
    }

    @Test
    fun `send reminder calls use case and sends other view event`() {
        whenever(sendBulkReminder.execute(listOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id))).thenReturn(
            Completable.error(Throwable("other_exception"))
        )

        pushIntent(Intent.SubmitClicked(listOf(TestData.CUSTOMER.id, TestData.CUSTOMER_2.id)))
        assert(lastViewEvent() is ViewEvent.ShowError)
    }

    private fun Customer.toBulkReminderItem(checked: Boolean) = BulkReminderItem(
        customerId = this.id,
        profilePic = profileImage,
        name = description,
        amountDue = balanceV2,
        checked = checked
    )
}
