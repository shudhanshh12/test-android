package `in`.okcredit.merchant.usecase

import `in`.okcredit.backend.contract.GetCustomerAccountNetBalance
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import merchant.okcredit.supplier.contract.GetSupplierAccountNetBalance
import org.junit.Test

class GetNetBalanceForBusinessTest {
    private val getCustomerAccountNetBalance: GetCustomerAccountNetBalance = mock()
    private val getSupplierAccountNetBalance: GetSupplierAccountNetBalance = mock()

    private val netBalanceForBusiness = GetNetBalanceForBusiness(
        { getCustomerAccountNetBalance },
        { getSupplierAccountNetBalance }
    )

    @Test
    fun `execute should return total net balance`() {
        val customerNetBalance = -78900L
        val supplierNetBalance = 90000L
        val businessId = "businessId"
        whenever(getCustomerAccountNetBalance.getNetBalance(businessId)).thenReturn(Observable.just(customerNetBalance))
        whenever(getSupplierAccountNetBalance.getNetBalance(businessId)).thenReturn(Observable.just(supplierNetBalance))

        val testObserver = netBalanceForBusiness.execute(businessId).test()

        testObserver.assertValue(customerNetBalance + supplierNetBalance)
        testObserver.dispose()
    }
}
