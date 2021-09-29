package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection.CustomerScreenSortSelection
import `in`.okcredit.merchant.customer_ui.usecase.GetCustomerScreenSortSelection.CustomerScreenSortSelection.*
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction
import timber.log.Timber
import javax.inject.Inject

class GetCustomerStatement @Inject constructor(
    private val getCustomer: Lazy<GetCustomer>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val getTxnDetailsUsingCollectionId: Lazy<GetTxnDetailsUsingCollectionId>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<GetCustomerStatement.Request, GetCustomerStatement.Response> {

    companion object {
        const val FROM_DEEP_LINK = "deep_link"
    }

    data class Request(
        val customerId: String?,
        val sourceScreen: String?,
        val collectionId: String?,
        val customerScreenSortSelection: CustomerScreenSortSelection,
    )

    data class Response(
        val customerStatement: List<Transaction>,
        val transaction: Transaction?,
        val lastIndexOfZeroBalanceDue: Int,
    )

    class TransactionNotFoundException : Exception()

    override fun execute(req: Request): Observable<Result<Response>> =
        UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                if (isOpenedFromDeepLink(req)) {
                    getResponseWhenUserFromDeepLink(req, businessId)
                } else {
                    getCustomer.get().execute(req.customerId)
                        .distinct { it.txnStartTime } // as we are only using start time we will emit only when it is changed
                        .flatMap {
                            // TODO: Merchant, Why txnStartTime is Noy nullable and not DateTime?
                            getTransactionList(req.customerId ?: "", it.txnStartTime ?: 0L, businessId, req.customerScreenSortSelection)
                                .map { transactions ->
                                    Timber.d("transactions map")
                                    createCustomerStatement(transactions, null)
                                }
                        }
                }
            }
        )

    private fun getResponseWhenUserFromDeepLink(req: Request, businessId: String): Observable<Response> {
        return getTxnDetailsUsingCollectionId.get().execute(req.collectionId)
            .flatMap {
                return@flatMap getCustomer.get().execute(it.customer.id)
                    .flatMap { customer ->
                        // TODO: Merchant, Why txnStartTime is Noy nullable and not DateTime?
                        getTransactionList(it.customer.id, customer.txnStartTime ?: 0L, businessId, req.customerScreenSortSelection)
                            .map { transactions ->
                                createCustomerStatement(transactions, it.transaction)
                            }
                    }
            }
    }

    private fun getTransactionList(
        customerId: String,
        txnStartTime: Long,
        businessId: String,
        customerScreenSortSelection: CustomerScreenSortSelection,
    ): Observable<List<Transaction>> {
        return when (customerScreenSortSelection) {
            CREATE_DATE -> transactionRepo.get().listTransactions(customerId, txnStartTime, businessId)
            BILL_DATE -> transactionRepo.get().listTransactionsSortedByBillDate(customerId, txnStartTime, businessId)
        }
    }

    private fun isOpenedFromDeepLink(req: Request) = req.sourceScreen == FROM_DEEP_LINK

    private fun createCustomerStatement(allTransactions: List<Transaction>, redirectTxn: Transaction?): Response {
        var currentDue: Long = 0
        var lastIndexOfZeroBalanceDue = 0
        allTransactions.forEachIndexed { index, transaction ->
            if (transaction.transactionState == Transaction.PROCESSING) {
                transaction.currentDue = currentDue
            } else if (transaction.type == Transaction.CREDIT) {
                if (transaction.isDeleted) {
                    transaction.currentDue = currentDue
                } else {
                    currentDue += transaction.amountV2
                    transaction.currentDue = currentDue
                }
            } else if (transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) {
                if (transaction.isDeleted) {
                    transaction.currentDue = currentDue
                } else {
                    currentDue -= transaction.amountV2
                    transaction.currentDue = currentDue
                }
            }
            if (currentDue == 0L &&
                !transaction.isDeleted &&
                index != allTransactions.size - 1
            ) {
                lastIndexOfZeroBalanceDue = index
            }
        }
        return Response(allTransactions, redirectTxn, lastIndexOfZeroBalanceDue)
    }
}
