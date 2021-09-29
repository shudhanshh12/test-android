package `in`.okcredit.frontend.usecase.supplier

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.junit.Test

class GetSupplierTransactionTest {

    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getSupplierTransaction =
        GetSupplierTransaction({ supplierCreditRepository }, { getActiveBusinessId })
    val businessId = "businessId"

    @Test
    fun `should call getTransaction method of repository`() {
        val transaction = Transaction(
            "1010",
            "supplier_id_1",
            null,
            true,
            10,
            null,
            null,
            DateTime.now(),
            DateTime.now(),
            createdBySupplier = false,
            deleted = false,
            deleteTime = DateTime.now(),
            deletedBySupplier = false,
            updateTime = DateTime.now(),
            syncing = false,
            lastSyncTime = DateTime.now(),
            transactionState = -1
        )
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.getTransaction("1010", businessId)).thenReturn(
            Observable.just(
                transaction
            )
        )

        val result = getSupplierTransaction.execute("1010").test()

        verify(supplierCreditRepository).getTransaction("1010", businessId)
        result.assertValue(transaction)
    }
}
