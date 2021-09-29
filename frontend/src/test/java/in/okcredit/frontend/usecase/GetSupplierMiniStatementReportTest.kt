package `in`.okcredit.frontend.usecase

import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsContract
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.junit.Test

class GetSupplierMiniStatementReportTest {

    private val supplierCreditRepository: SupplierCreditRepository = mock()
    private val getAllTransactionsForSupplier: GetAllTransactionsForSupplier = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val time = DateTime.now()
    val selectedDateMode = SupplierReportsContract.SelectedDateMode.LAST_ZERO_BALANCE
    private val getSupplierMiniStatementReport =
        GetSupplierMiniStatementReport({ supplierCreditRepository }, { getAllTransactionsForSupplier }, { getActiveBusinessId })

    @Test
    fun `getSupplierMiniStatementReport execute() should return GetSupplierMiniStatementReport_Responnse on valid input`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(supplierCreditRepository.getSupplier("1", businessId)).thenReturn(getMockSupplier())
        whenever(getAllTransactionsForSupplier.execute("1", selectedDateMode)).thenReturn(
            Single.just(
                GetAllTransactionsForSupplier.Response(
                    SupplierReportsContract.SupplierStatementResponse(getDummyTransactionsList(), 1, 0, 10, 10, 0),
                    SupplierReportsContract.SelectedDateMode.LAST_ZERO_BALANCE, time, time
                )
            )
        )
        whenever(supplierCreditRepository.listTransactions("1", businessId)).thenReturn(Observable.just(getDummyTransactionsList()))
        val observable = getSupplierMiniStatementReport.execute("1").test()
        observable.assertValue(
            GetSupplierMiniStatementReport.Response(
                SupplierReportsContract.SupplierStatementResponse(getDummyTransactionsList(), 1, 0, 10, 10, 0),
                SupplierReportsContract.SelectedDateMode.LAST_ZERO_BALANCE, time, time
            )
        )
        observable.dispose()
    }

    fun getDummyTransactionsList(): MutableList<Transaction> {
        val list = mutableListOf<Transaction>()
        list.add(
            Transaction(
                "1",
                "supplier_id_1",
                null,
                true,
                10,
                null,
                null,
                time,
                time,
                false,
                false,
                time,
                false,
                time,
                false,
                time,
                -1
            )
        )
        list.add(
            Transaction(
                "2",
                "supplier_id_1",
                null,
                true,
                10,
                null,
                null,
                time,
                time,
                false,
                false,
                time,
                false,
                time,
                false,
                time,
                -1
            )
        )
        return list
    }

    private fun getMockSupplier(): Observable<Supplier>? {
        return Observable.just(
            Supplier(
                "1", false,
                false,
                time,
                0L,
                "abc",
                null,
                null,
                null,
                1,
                0,
                null,
                null,
                true,
                null,
                false,
                null,
                false,
                Supplier.ACTIVE,
                false,
                false
            )
        )
    }
}
