package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetAllDueCustomersByLastPaymentTest {

    private val customerRepo: CustomerRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getAllDueCustomersByLastPayment = GetAllDueCustomersByLastPayment(customerRepo, { getActiveBusinessId })

    @Test
    fun `should return customer list by last payment date`() {
        val customers = listOf<Customer>()
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.listCustomersByLastPayment(businessId)).thenReturn(Observable.just(customers))

        val testObserver = getAllDueCustomersByLastPayment.execute().test()

        testObserver.assertValue(customers)
    }
}
