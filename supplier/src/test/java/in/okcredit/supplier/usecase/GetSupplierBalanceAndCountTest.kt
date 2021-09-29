package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.supplier.statement.usecase.GetSupplierBalanceAndCount
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetSupplierBalanceAndCountTest {

    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getSupplierSummary = GetSupplierBalanceAndCount(
        { supplierCreditRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `execute test`() {
        // given
        val balance = 900L
        val count = 5
        val list = listOf(
            mock<Supplier>().apply {
                whenever(this.deleted).thenReturn(false)
                whenever(this.state).thenReturn(Supplier.ACTIVE)
                whenever(this.balance).thenReturn(-1000)
            },
            mock<Supplier>().apply {
                whenever(this.deleted).thenReturn(false)
                whenever(this.state).thenReturn(Supplier.ACTIVE)
                whenever(this.balance).thenReturn(2000)
            },
            mock<Supplier>().apply {
                whenever(this.deleted).thenReturn(false)
                whenever(this.state).thenReturn(Supplier.ACTIVE)
                whenever(this.balance).thenReturn(3000)
            },
            mock<Supplier>().apply {
                whenever(this.deleted).thenReturn(false)
                whenever(this.state).thenReturn(Supplier.ACTIVE)
                whenever(this.balance).thenReturn(-5000)
            },
            mock<Supplier>().apply {
                whenever(this.deleted).thenReturn(false)
                whenever(this.state).thenReturn(Supplier.ACTIVE)
                whenever(this.balance).thenReturn(1900)
            },
            mock<Supplier>().apply {
                whenever(this.deleted).thenReturn(true)
                whenever(this.state).thenReturn(Supplier.ACTIVE)
                whenever(this.balance).thenReturn(0)
            },
            mock<Supplier>().apply {
                whenever(this.deleted).thenReturn(false)
                whenever(this.state).thenReturn(Supplier.BLOCKED)
                whenever(this.balance).thenReturn(2000)
            }
        )
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.getSuppliers(businessId)).thenReturn(Observable.just(list))

        // when
        val result = getSupplierSummary.execute().test()

        result.assertValues(
            GetSupplierBalanceAndCount.Response(balance, count)
        )
    }
}
