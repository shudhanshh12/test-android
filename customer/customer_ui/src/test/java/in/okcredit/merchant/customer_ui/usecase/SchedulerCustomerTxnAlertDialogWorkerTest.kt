package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.usecase.CustomerTxnAlertDialogDismissWorker
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class SchedulerCustomerTxnAlertDialogWorkerTest {

    private lateinit var schedulerCustomerTxnALertDialogWorker: SchedulerCustomerTxnALertDialogWorker

    private val customerTxnAlertDialogDismissWorker: CustomerTxnAlertDialogDismissWorker = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()

        schedulerCustomerTxnALertDialogWorker =
            SchedulerCustomerTxnALertDialogWorker({ customerTxnAlertDialogDismissWorker }, { getActiveBusinessId })
    }

    @Test
    fun `execute calls dismiss worker schedule`() {
        val request = SchedulerCustomerTxnALertDialogWorker.Request("1231")
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(
            customerTxnAlertDialogDismissWorker.schedule(
                request.accountID,
                businessId
            )
        ).thenReturn(Completable.complete())

        val testObserver = schedulerCustomerTxnALertDialogWorker.execute(request).test()

        verify(customerTxnAlertDialogDismissWorker).schedule(request.accountID, businessId)

        assert(testObserver.values().last() == Result.Success(Unit))

        testObserver.dispose()
    }
}
