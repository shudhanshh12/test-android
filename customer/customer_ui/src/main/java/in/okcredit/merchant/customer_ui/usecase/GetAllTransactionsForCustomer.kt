package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract
import `in`.okcredit.merchant.customer_ui.utils.CustomerUtils
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import javax.inject.Inject

class GetAllTransactionsForCustomer @Inject constructor(
    private val getCustomer: Lazy<GetCustomer>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    data class Response(
        val customerStatementResponse: CustomerReportsContract.CustomerStatementResponse,
        val selectedDateMode: CustomerReportsContract.SelectedDateMode,
        val startDate: DateTime,
        val endDate: DateTime
    )

    fun execute(customerId: String, selectedDateMode: CustomerReportsContract.SelectedDateMode): Observable<Response> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            getCustomer.get().execute(customerId)
                .distinct { it.txnStartTime } // as we are only using start time we will emit only when it is changed
                .flatMap { customer ->
                    getTransactions(customerId, customer.txnStartTime ?: 0L, businessId)
                        .map { transactions -> CustomerUtils.getCustomerStatement(transactions) }
                        .map { customerStatement ->
                            Response(
                                customerStatementResponse = customerStatement,
                                selectedDateMode = selectedDateMode,
                                startDate = customerStatement.transactions.last().billDate.minusMillis(1), // to support after
                                endDate = DateTime.now()
                            )
                        }
                }
        }
    }

    private fun getTransactions(
        customerId: String,
        txnStartTime: Long,
        businessId: String
    ): Observable<List<Transaction>> {

        return transactionRepo.get().listTransactions(customerId, txnStartTime, businessId)
            .map { transactions ->
                transactions.filter { it.transactionState != Transaction.PROCESSING && it.isDeleted.not() }
                    .asReversed()
            }
    }
}
