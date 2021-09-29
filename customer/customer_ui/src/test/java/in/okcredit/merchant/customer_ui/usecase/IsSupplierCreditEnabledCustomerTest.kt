package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class IsSupplierCreditEnabledCustomerTest {
    private val keyValService: KeyValService = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val isSupplierCreditEnabledCustomer =
        IsSupplierCreditEnabledCustomer({ keyValService }, { getActiveBusinessId })
    private val businessId = "businessId"

    @Before
    fun setup() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
    }

    @Test
    fun `IsSupplierCreditEnabledCustomer() return true`() {
        whenever(keyValService.contains(eq("key_sc_enabled_customer_ids"), any())).thenReturn(Single.just(true))
        whenever(keyValService.get(eq("key_sc_enabled_customer_ids"), any())).thenReturn(Observable.just("abc"))

        val testObserver = isSupplierCreditEnabledCustomer.execute("abc").test()
        testObserver.assertValues(
            true
        )
        testObserver.dispose()
    }

    @Test
    fun `IsSupplierCreditEnabledCustomer() return false when key is different`() {
        whenever(keyValService.contains(eq("key_sc_enabled_customer_ids"), any())).thenReturn(Single.just(true))
        whenever(keyValService.get(eq("key_sc_enabled_customer_ids"), any())).thenReturn(Observable.just("abc"))

        val testObserver = isSupplierCreditEnabledCustomer.execute("abc123").test()
        testObserver.assertValues(
            false
        )
        testObserver.dispose()
    }

    @Test
    fun `IsSupplierCreditEnabledCustomer() return false and keyValService return false`() {
        whenever(keyValService.contains(eq("key_sc_enabled_customer_ids"), any())).thenReturn(Single.just(false))

        val testObserver = isSupplierCreditEnabledCustomer.execute("abc").test()
        testObserver.assertValues(
            false
        )
        testObserver.dispose()
    }
}
