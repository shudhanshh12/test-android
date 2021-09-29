package `in`.okcredit.frontend.usecase.supplier

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class DeleteSupplierTest {
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val deleteSupplier =
        DeleteSupplier({ supplierCreditRepository }, { getActiveBusinessId })

    @Test
    fun `should delete supplier `() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.deleteSupplier("123", businessId)).thenReturn(Completable.complete())
        val result = deleteSupplier.execute("123").test()
        verify(supplierCreditRepository).deleteSupplier("123", businessId)
        result.assertComplete()
    }
}
