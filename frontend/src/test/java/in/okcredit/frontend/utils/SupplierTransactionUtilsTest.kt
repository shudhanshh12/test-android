package `in`.okcredit.frontend.utils

import `in`.okcredit.merchant.suppliercredit.Transaction
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test

class SupplierTransactionUtilsTest {

    @Test
    fun `getSupplierStatement() should return SupplierStatementResponse when provided with transactions`() {
        val repsonse = SupplierTransactionUtils.getSupplierStatement(getDummyTransactionsList())
        Assert.assertEquals(2, repsonse.transactions.size)
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
                DateTime.now(),
                DateTime.now(),
                false,
                false,
                DateTime.now(),
                false,
                DateTime.now(),
                false,
                DateTime.now(),
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
                DateTime.now(),
                DateTime.now(),
                false,
                false,
                DateTime.now(),
                false,
                DateTime.now(),
                false,
                DateTime.now(),
                -1
            )
        )
        return list
    }

    @Test
    fun `getSupplierStatement() should remove  deleted transactions from respose`() {
        val repsonse = SupplierTransactionUtils.getSupplierStatement(getDummyMixedDeletedTransactionsList())
        Assert.assertEquals(1, repsonse.transactions.size)
    }

    private fun getDummyMixedDeletedTransactionsList(): List<Transaction> {
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
                DateTime.now(),
                DateTime.now(),
                false,
                true,
                DateTime.now(),
                false,
                DateTime.now(),
                false,
                DateTime.now(),
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
                DateTime.now(),
                DateTime.now(),
                false,
                false,
                DateTime.now(),
                false,
                DateTime.now(),
                false,
                DateTime.now(),
                -1
            )
        )
        return list
    }
}
