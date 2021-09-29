package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class DoDueCustomersExistsTest {

    private val customerRepo: CustomerRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val doDueCustomersExists = DoDueCustomersExists(customerRepo) { getActiveBusinessId }

    @Test
    fun `should return true if due customer exists with balance less than -10, status = 1 and non-empty mobile`() {
        val customer =
            Gson().fromJson("{\"mobile\":\"8882946897\",\"balanceV2\":-2000,\"status\":1}", Customer::class.java)
        val customers = listOf(customer)
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.listCustomers(businessId)).thenReturn(Observable.just(customers))

        val testObserver = doDueCustomersExists.execute().test()

        testObserver.assertValue(true)
        verify(customerRepo).listCustomers(businessId)

        testObserver.dispose()
    }

    @Test
    fun `should return false if due customer exists with balance equals -10, status = 1 and non-empty mobile`() {
        val customer =
            Gson().fromJson("{\"mobile\":\"8882946897\",\"balanceV2\":-1000,\"status\":1}", Customer::class.java)
        val customers = listOf(customer)
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.listCustomers(businessId)).thenReturn(Observable.just(customers))

        val testObserver = doDueCustomersExists.execute().test()

        testObserver.assertValue(false)
        verify(customerRepo).listCustomers(businessId)

        testObserver.dispose()
    }

    @Test
    fun `should return false if due customer exists with balance greater than -10, status = 1 and non-empty mobile`() {
        val customer =
            Gson().fromJson("{\"mobile\":\"8882946897\",\"balanceV2\":-900,\"status\":1}", Customer::class.java)
        val customers = listOf(customer)
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.listCustomers(businessId)).thenReturn(Observable.just(customers))

        val testObserver = doDueCustomersExists.execute().test()

        testObserver.assertValue(false)
        verify(customerRepo).listCustomers(businessId)

        testObserver.dispose()
    }

    @Test
    fun `should return false when due customer dont exists`() {
        val customers = listOf<Customer>()

        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.listCustomers(businessId)).thenReturn(Observable.just(customers))

        val testObserver = doDueCustomersExists.execute().test()

        testObserver.assertValue(false)
        verify(customerRepo).listCustomers(businessId)

        testObserver.dispose()
    }
}
