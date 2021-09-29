package `in`.okcredit.supplier.home.tab

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import merchant.okcredit.accounting.contract.HomeSortType
import org.junit.Test

class GetSupplierSortTypeTest {

    private val repository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"
    private val getSupplierSortType = GetSupplierSortType({ repository }, { getActiveBusinessId })

    @Test
    fun `execute method should return the value returned by repository`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(repository.getSortType(businessId)).thenReturn(Observable.just(HomeSortType.NAME))

        val type = getSupplierSortType.execute().test()

        type.assertValue(HomeSortType.NAME)
    }
}
