package `in`.okcredit.frontend.usecase

import `in`.okcredit.frontend.ui.supplier_reports.SupplierReportsContract
import `in`.okcredit.frontend.utils.SupplierTransactionUtils
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import dagger.Lazy
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

class GetSupplierMiniStatementReport @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getAllTransactionsForSupplier: Lazy<GetAllTransactionsForSupplier>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    data class Response(
        val supplierStatementResponse: SupplierReportsContract.SupplierStatementResponse,
        val selectedDateMode: SupplierReportsContract.SelectedDateMode,
        val startDate: DateTime,
        val endDate: DateTime
    )

    fun execute(supplierId: String): Single<Response> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            supplierCreditRepository.get().getSupplier(supplierId, businessId)
                .firstOrError()
                .flatMap {
                    supplierCreditRepository.get().listTransactions(supplierId, businessId)
                        .firstOrError()
                        .map { transactions ->
                            val sortedTransactions = transactions.sortedByDescending { it.billDate }
                            val nonDeletedTransactions =
                                sortedTransactions.filter { transaction -> transaction.deleted.not() }
                            return@map getResponse(it, nonDeletedTransactions)
                        }
                }
        }
    }

    private fun getResponse(
        supplier: Supplier,
        nonDeletedTransactions: List<Transaction>
    ): Response {

        var runningBalance: Long = 0
        val runningBalanceTxn = mutableListOf<Transaction>()

        for (transaction in nonDeletedTransactions.reversed()) {
            if (transaction.transactionState != Transaction.Constants.PROCESSING && transaction.deleted.not()) {
                if (transaction.payment.not()) {
                    runningBalance -= transaction.amount
                } else {
                    runningBalance += transaction.amount
                }

                if (runningBalance == 0L) {
                    runningBalanceTxn.add(transaction)
                }
            }
        }

        val selectedDateMode = SupplierReportsContract.SelectedDateMode.LAST_ZERO_BALANCE
        if (runningBalanceTxn.isNotEmpty()) {
            return getMiniStatementTransactions(runningBalanceTxn, supplier, nonDeletedTransactions, selectedDateMode)
        } else {
            return getAllTransactionsForSupplier.get().execute(supplier.id, selectedDateMode)
                .map {
                    return@map Response(
                        supplierStatementResponse = it.supplierStatementResponse,
                        selectedDateMode = selectedDateMode,
                        startDate = it.startDate,
                        endDate = it.endDate
                    )
                }.blockingGet()
        }
    }

    private fun getMiniStatementTransactions(
        runningBalanceTxn: MutableList<Transaction>,
        supplier: Supplier,
        nonDeletedTransactions: List<Transaction>,
        selectedDateMode: SupplierReportsContract.SelectedDateMode
    ): Response {

        val startDate = SupplierTransactionUtils.getTxnStartTime(supplier, runningBalanceTxn.last().billDate)

        val miniStmtTxn = nonDeletedTransactions.filter { transaction -> transaction.billDate > startDate }

        return Response(
            supplierStatementResponse = SupplierTransactionUtils.getSupplierStatement(miniStmtTxn),
            selectedDateMode = selectedDateMode,
            startDate = startDate.plusSeconds(1), // added so that report is created after zero'th transaction
            endDate = DateTime.now()
        )
    }
}
