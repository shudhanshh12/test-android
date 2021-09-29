package `in`.okcredit.frontend.usecase

import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsContract
import `in`.okcredit.frontend.utils.SupplierTransactionUtils
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

class GetAllTransactionsForSupplier @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    data class Response(
        val supplierStatementResponse: SupplierReportsContract.SupplierStatementResponse,
        val selectedDateMode: SupplierReportsContract.SelectedDateMode,
        val startDate: DateTime,
        val endDate: DateTime
    )

    fun execute(supplierId: String, selectedDateMode: SupplierReportsContract.SelectedDateMode): Single<Response> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            supplierCreditRepository.get().getSupplier(supplierId, businessId)
                .firstOrError()
                .flatMap { customer ->

                    var txnStartTime = 0L
                    txnStartTime = customer.txnStartTime

                    getTransactions(supplierId, txnStartTime, businessId)
                        .firstOrError()
                        .map { transactions ->
                            Response(
                                supplierStatementResponse = SupplierTransactionUtils.getSupplierStatement(transactions),
                                selectedDateMode = selectedDateMode,
                                startDate = transactions.last().billDate.minusMillis(1), // to support after
                                endDate = DateTime.now()
                            )
                        }
                }
        }
    }

    private fun getTransactions(
        supplierId: String,
        txnStartTime: Long,
        businessId: String
    ): Observable<List<Transaction>> {
        return supplierCreditRepository.get().listTransactions(supplierId, txnStartTime, businessId)
            .map { transactions ->
                transactions.filter { it.transactionState != Transaction.Constants.PROCESSING && it.deleted.not() }
            }
    }
}
