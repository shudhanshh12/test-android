package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract
import `in`.okcredit.merchant.customer_ui.utils.CustomerUtils
import dagger.Lazy
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import javax.inject.Inject

class GetMiniStatementReport @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val getCustomer: GetCustomer,
    private val getAllTransactionsForCustomer: Lazy<GetAllTransactionsForCustomer>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    data class Response(
        val customerStatementResponse: CustomerReportsContract.CustomerStatementResponse,
        val selectedDateMode: CustomerReportsContract.SelectedDateMode,
        val startDate: DateTime,
        val endDate: DateTime
    )

    fun execute(customerId: String): Single<Response> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            getCustomer.execute(customerId)
                .firstOrError()
                .flatMap { customer ->
                    transactionRepo.listNonDeletedTransactionsByBillDate(customerId, businessId)
                        .firstOrError()
                        .map { transactions ->
                            return@map getResponse(customer, transactions)
                        }
                }
        }
    }

    private fun getResponse(
        customer: Customer,
        nonDeletedTransactions: List<Transaction>
    ): Response {

        var runningBalance: Long = 0
        val runningBalanceTxn = mutableListOf<Transaction>()

        for (transaction in nonDeletedTransactions.reversed()) {
            if (transaction.type == Transaction.CREDIT) {
                runningBalance -= transaction.amountV2
            } else if (transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) {
                runningBalance += transaction.amountV2
            }
            if (runningBalance == 0L) {
                runningBalanceTxn.add(transaction)
            }
        }

        val selectedDateMode = CustomerReportsContract.SelectedDateMode.LAST_ZERO_BALANCE
        if (runningBalanceTxn.isNotEmpty()) {
            return getMiniStatementTransactions(runningBalanceTxn, customer, nonDeletedTransactions, selectedDateMode)
        } else {
            return getAllTransactionsForCustomer.get().execute(customer.id, selectedDateMode)
                .map {
                    return@map Response(
                        customerStatementResponse = it.customerStatementResponse,
                        selectedDateMode = selectedDateMode,
                        startDate = it.startDate,
                        endDate = it.endDate
                    )
                }.blockingFirst()
        }
    }

    private fun getMiniStatementTransactions(
        runningBalanceTxn: MutableList<Transaction>,
        customer: Customer,
        nonDeletedTransactions: List<Transaction>,
        selectedDateMode: CustomerReportsContract.SelectedDateMode
    ): Response {
        val startDate = CustomerUtils.getTxnStartTime(customer, runningBalanceTxn.last().billDate)

        val miniStmtTxns =
            nonDeletedTransactions.filter { transaction -> transaction.billDate.millis > startDate.millis }

        return Response(
            customerStatementResponse = CustomerUtils.getCustomerStatement(miniStmtTxns),
            selectedDateMode = selectedDateMode,
            startDate = startDate.plusSeconds(1), // added so that report is created after zero'th transaction
            endDate = DateTime.now()
        )
    }
}
