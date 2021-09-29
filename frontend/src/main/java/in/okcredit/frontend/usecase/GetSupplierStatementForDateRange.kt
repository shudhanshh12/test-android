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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetSupplierStatementForDateRange @Inject constructor(
    private val supplierCreditRepository: SupplierCreditRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    data class Request(
        val supplierId: String,
        val startTime: DateTime,
        val endTime: DateTime
    )

    fun execute(request: Request): Single<SupplierReportsContract.SupplierStatementResponse> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            supplierCreditRepository.getSupplier(request.supplierId, businessId)
                .firstOrError()
                .flatMap { supplier ->

                    var supplierTxnStartTime = DateTime(0)
                    supplierTxnStartTime = DateTime(TimeUnit.SECONDS.toMillis(supplier.txnStartTime))
                    getTransactions(
                        request.supplierId,
                        supplierTxnStartTime,
                        request.startTime,
                        request.endTime,
                        businessId
                    ).firstOrError()
                        .map { transactions ->
                            SupplierTransactionUtils.getSupplierStatement(transactions)
                        }
                }
        }
    }

    private fun getTransactions(
        supplierId: String,
        supplierTxnStartTime: DateTime,
        startTime: DateTime,
        endTime: DateTime,
        businessId: String
    ): Observable<List<Transaction>> {

        val startTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(startTime.millis)
        val endTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(endTime.millis)
        val customerTxnStartTimeInMilliSec = TimeUnit.MILLISECONDS.toSeconds(supplierTxnStartTime.millis)

        return supplierCreditRepository.listSupplierTransactionsBetweenBillDate(
            supplierId,
            customerTxnStartTimeInMilliSec,
            startTimeInMilliSec,
            endTimeInMilliSec,
            businessId
        ).map { transactions ->
            transactions.filter { it.transactionState != Transaction.Constants.PROCESSING && it.deleted.not() }
        }
    }
}
