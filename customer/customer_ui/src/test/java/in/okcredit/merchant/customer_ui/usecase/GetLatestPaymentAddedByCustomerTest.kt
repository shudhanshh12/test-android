package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import org.junit.Test

class GetLatestPaymentAddedByCustomerTest {
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val transactionRepo: TransactionRepo = mock()

    private val getLatestPaymentAddedByCustomer = GetLatestPaymentAddedByCustomer(
        { transactionRepo },
        { getActiveBusinessId }
    )

    @Test
    fun `execute should return transaction`() {
        val customerId = "customer-id"
        val businessId = "business-id"
        val transaction: Transaction = mock()
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(transactionRepo.getLatestPaymentAddedByCustomer(customerId, businessId))
            .thenReturn(Single.just(transaction))

        val testObserver = getLatestPaymentAddedByCustomer.execute(customerId).test()

        verify(getActiveBusinessId).execute()
        verify(transactionRepo).getLatestPaymentAddedByCustomer(customerId, businessId)
        testObserver.assertValue(transaction)
    }
}
