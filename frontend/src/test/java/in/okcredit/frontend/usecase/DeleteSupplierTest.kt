package `in`.okcredit.frontend.usecase

import `in`.okcredit.frontend.usecase.supplier.DeleteSupplier
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.auth.usecases.VerifyPassword

class DeleteSupplierTest {

    private val verifyPassword: VerifyPassword = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val deleteSupplier = DeleteSupplier({ supplierCreditRepository }, { getActiveBusinessId })

    @Test
    fun `delete supplier`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(verifyPassword.execute("abc")).thenReturn(Completable.complete())
        whenever(supplierCreditRepository.deleteSupplier("123", businessId)).thenReturn(Completable.complete())

        val testObserver = deleteSupplier.execute("123").test()

        verify(supplierCreditRepository).deleteSupplier("123", businessId)
        testObserver.assertComplete()
    }
}
