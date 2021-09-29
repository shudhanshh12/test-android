package `in`.okcredit.merchant.customer_ui.utils

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract
import `in`.okcredit.shared.utils.CommonUtils
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class CustomerUtils {

    companion object {
        const val TAG = "CustomerUtils"

        fun isValidMobileNumber(mobileNumber: String) = mobileNumber.isNotBlank() && mobileNumber.length == 10

        fun getCustomerStatement(
            allTransactions: List<Transaction>
        ): CustomerReportsContract.CustomerStatementResponse {
            val sortedTransactions = sortTxns(allTransactions).filter { transaction -> transaction.isDeleted.not() }

            var paymentTransactionCount = 0
            var creditTransactionCount = 0
            var balanceForSelectedDuration: Long = 0
            var totalPayment: Long = 0
            var totalCredit: Long = 0

            for (transaction in sortedTransactions.reversed()) {
                if (transaction.transactionState != Transaction.PROCESSING) {
                    if (transaction.type == Transaction.CREDIT) {
                        creditTransactionCount++
                        totalCredit += transaction.amountV2
                        balanceForSelectedDuration -= transaction.amountV2
                    } else if (transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) {
                        paymentTransactionCount++
                        totalPayment += transaction.amountV2
                        balanceForSelectedDuration += transaction.amountV2
                    }
                }
            }

            return CustomerReportsContract.CustomerStatementResponse(
                sortedTransactions,
                paymentTransactionCount,
                creditTransactionCount,
                balanceForSelectedDuration,
                totalPayment,
                totalCredit
            )
        }

        fun sortTxns(transactions: List<Transaction>): List<Transaction> {
            return transactions.sortedByDescending { it.billDate.millis }
        }

        fun getTxnStartTime(customer: Customer, startDate: DateTime): DateTime {
            var newStartDateTime = startDate

            customer.txnStartTime?.let { txnStartTime ->
                if (txnStartTime > 0) {
                    val customerTxnStartTime = DateTime(TimeUnit.SECONDS.toMillis(customer.txnStartTime!!))

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
    }
}
