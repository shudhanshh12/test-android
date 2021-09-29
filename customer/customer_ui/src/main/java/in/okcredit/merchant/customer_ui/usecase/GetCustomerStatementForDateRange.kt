package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract
import `in`.okcredit.merchant.customer_ui.utils.CustomerUtils
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetCustomerStatementForDateRange @Inject constructor(
    private val getCustomer: GetCustomer,
    private val transactionRepo: TransactionRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    data class Request(
        val customerId: String,
        val startTime: DateTime,
        val endTime: DateTime,
    )

    fun execute(request: Request): Single<CustomerReportsContract.CustomerStatementResponse> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            getCustomer.execute(request.customerId)
                .firstOrError()
                .flatMap { customer ->

                    var customerTxnStartTime = DateTime(0)
                    if (customer.txnStartTime != null) {
                        customerTxnStartTime = DateTime(TimeUnit.SECONDS.toMillis(customer.txnStartTime!!))
                    }
                    getTransactions(
                        request.customerId,
                        customerTxnStartTime,
                        request.startTime,
                        request.endTime,
                        businessId
                    )
                        .firstOrError()
                        .map { transactions ->
                            CustomerUtils.getCustomerStatement(transactions)
                        }
                }
        }
    }

    private fun getTransactions(
        customerId: String,
        customerTxnStartTime: DateTime,
        startTime: DateTime,
        endTime: DateTime,
        businessId: String,
    ): Observable<List<merchant.okcredit.accounting.model.Transaction>> {

        val startTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(startTime.millis)
        val endTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(endTime.millis)
        val customerTxnStartTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(customerTxnStartTime.millis)

        return transactionRepo.listCustomerTransactionsBetweenBillDate(
            customerId,
            customerTxnStartTimeInMilliSec,
            startTimeInMilliSec,
            endTimeInMilliSec,
            businessId
        ).map { transactions ->
            transactions.filter { it.transactionState != merchant.okcredit.accounting.model.Transaction.PROCESSING && it.isDeleted.not() }
        }
    }
}
