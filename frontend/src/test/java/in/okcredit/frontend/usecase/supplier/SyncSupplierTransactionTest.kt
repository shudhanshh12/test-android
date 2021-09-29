package `in`.okcredit.frontend.usecase.supplier

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.joda.time.DateTime
import org.junit.Test

class SyncSupplierTransactionTest {

    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val syncSupplierTransaction =
        SyncSupplierTransaction({ supplierCreditRepository }, { getActiveBusinessId })

    @Test
    fun `should call sync transaction method of repository`() {
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
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.syncTransaction(transaction, businessId)).thenReturn(Single.just("1010"))

        val result = syncSupplierTransaction.execute(transaction).test()

        verify(supplierCreditRepository).syncTransaction(transaction, businessId)
        result.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success("1010")
        )
    }
}
