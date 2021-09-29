package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class ReactivateSupplierTest {
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getDefauiltBusinessId: GetActiveBusinessId = mock()
    private val reactivateSupplier = ReactivateSupplier(supplierCreditRepository, { getDefauiltBusinessId })

    companion object {
        private val supplierId = "suplierId"
        private val businessId = "business-id"
        private val name = "name"
        private val req = ReactivateSupplier.Request(name, supplierId)
    }

    @Test
    fun `test execute`() {
        // given
        whenever(getDefauiltBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.reactivateSupplier(supplierId, name, businessId)).thenReturn(Completable.complete())

        // when
        val result = reactivateSupplier.execute(req).test()

        result.assertComplete()
    }

    @Test
    fun `verify method call `() {
        // given
        whenever(getDefauiltBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.reactivateSupplier(supplierId, name, businessId)).thenReturn(Completable.complete())

        // when
        val result = reactivateSupplier.execute(req).test()

        // then
        verify(supplierCreditRepository).reactivateSupplier(req.supplierId, req.name, businessId)
    }
}
