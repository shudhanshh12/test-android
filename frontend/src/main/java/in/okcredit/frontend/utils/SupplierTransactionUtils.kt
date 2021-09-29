package `in`.okcredit.frontend.utils

import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsContract
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.shared.utils.CommonUtils
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit

object SupplierTransactionUtils {
    fun getSupplierStatement(
        allTransactions: List<Transaction>
    ): SupplierReportsContract.SupplierStatementResponse {
        val sortedTransactions = sortTxns(
            allTransactions
        ).filter { transaction -> transaction.deleted.not() }

        var paymentTransactionCount = 0
        var creditTransactionCount = 0
        var balanceForSelectedDuration: Long = 0
        var totalPayment: Long = 0
        var totalCredit: Long = 0

        for (transaction in sortedTransactions.reversed()) {
            if (transaction.transactionState != Transaction.Constants.PROCESSING) {
                if (transaction.payment.not()) {
                    creditTransactionCount++
                    totalCredit += transaction.amount
                    balanceForSelectedDuration -= transaction.amount
                } else {
                    paymentTransactionCount++
                    totalPayment += transaction.amount
                    balanceForSelectedDuration += transaction.amount
                }
            }
        }

        return SupplierReportsContract.SupplierStatementResponse(
            sortedTransactions,
            paymentTransactionCount,
            creditTransactionCount,
            balanceForSelectedDuration,
            totalPayment,
            totalCredit
        )
    }

    fun sortTxns(transactions: List<Transaction>): List<Transaction> {
        Collections.sort(transactions, BillDate())
        return transactions
    }

    fun getTxnStartTime(supplier: Supplier, startDate: DateTime): DateTime {
        var newStartDateTime = startDate

        supplier.txnStartTime?.let { txnStartTime ->
            if (txnStartTime > 0) {
                val customerTxnStartTime = DateTime(TimeUnit.SECONDS.toMillis(supplier.txnStartTime))

                if (newStartDateTime.isBefore(customerTxnStartTime)) {
                    newStartDateTime = customerTxnStartTime
                }
            }
        }

        return newStartDateTime
    }

    fun getStartDateOfLastMonth() = CommonUtils.currentDateTime().withTimeAtStartOfDay()
        .minusMonths(1).withDayOfMonth(1)

    fun getStartDateOfThreeMonthBefore() = CommonUtils.currentDateTime().withTimeAtStartOfDay()
        .minusMonths(3).withDayOfMonth(1)

    fun getStartDateOfSixMonthBefore() = CommonUtils.currentDateTime().withTimeAtStartOfDay()
        .minusMonths(6).withDayOfMonth(1)

    fun getStartDateOfWeekBefore() = CommonUtils.currentDateTime().withTimeAtStartOfDay().minusWeeks(1)

    fun getStartDateOfThisMonth() = CommonUtils.currentDateTime().withTimeAtStartOfDay().withDayOfMonth(1)

    class BillDate : Comparator<Transaction> {
        override fun compare(c1: Transaction, c2: Transaction): Int {
            val b1 = Math.abs(c1.billDate.millis)
            val b2 = Math.abs(c2.billDate.millis)
            return when {
                b1 > b2 -> -1
                b1 < b2 -> 1
                else -> 0
            }
        }
    }
}
