package `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract
import `in`.okcredit.merchant.customer_ui.usecase.GetAllTransactionsForCustomer
import `in`.okcredit.merchant.customer_ui.utils.CustomerUtils
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class GetAllTransactionsForCustomerTest {
    private val getCustomer: GetCustomer = mock()
    private val transactionRepo: TransactionRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private lateinit var getAllTransactionsForCustomer: GetAllTransactionsForCustomer

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()

        getAllTransactionsForCustomer = GetAllTransactionsForCustomer({ getCustomer }, { transactionRepo }, { getActiveBusinessId })
    }

    @Test
    fun execute() {
        mockkStatic(DateTime::class)
        every { DateTime.now() } returns DateTime(100)

        val sampleResponse = mutableListOf(
            TestData.TRANSACTION1,
            TestData.TRANSACTION2,
            TestData.TRANSACTION3
        )
        whenever(getCustomer.execute(TestData.CUSTOMER.id)).thenReturn(Observable.just(TestData.CUSTOMER))

        whenever(transactionRepo.listTransactions(TestData.CUSTOMER.id, TestData.CUSTOMER.txnStartTime!!, TestData.BUSINESS_ID))
            .thenReturn(
                Observable.just(
                    sampleResponse
                )
            )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
        val testObserver = getAllTransactionsForCustomer.execute(
            TestData.CUSTOMER.id, CustomerReportsContract.SelectedDateMode.CUSTOM_DATE
        ).test()

        verify(getActiveBusinessId).execute()
        verify(getCustomer).execute(TestData.CUSTOMER.id)
        verify(transactionRepo).listTransactions(TestData.CUSTOMER.id, TestData.CUSTOMER.txnStartTime!!, TestData.BUSINESS_ID)
        val expectedCustomerStatement = CustomerUtils.getCustomerStatement(sampleResponse)
        assert(
            testObserver.values().last() == GetAllTransactionsForCustomer.Response(
                customerStatementResponse = expectedCustomerStatement,
                selectedDateMode = CustomerReportsContract.SelectedDateMode.CUSTOM_DATE,
                startDate = sampleResponse.last().billDate.minusMillis(1),
                endDate = DateTime(100)
            )
        )
    }
}
