package `in`.okcredit.frontend.usecase.supplier

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.auth.usecases.VerifyPassword

class DeleteSupplierTransactionTest {

    private val verifyPassword: VerifyPassword = mock()
    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val deleteSupplierTransaction =
        DeleteSupplierTransaction({ verifyPassword }, { supplierCreditRepository }, { getActiveBusinessId })

    @Test
    fun `should execute verifyPassword usecase and then delete transaction`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(verifyPassword.execute("000000")).thenReturn(Completable.complete())
        whenever(supplierCreditRepository.deleteTransaction("123", businessId)).thenReturn(Completable.complete())

        val result = deleteSupplierTransaction.execute("123").test()

        verify(supplierCreditRepository).deleteTransaction("123", businessId)
        result.assertComplete()
    }
}
