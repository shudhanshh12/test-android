package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetTxnDetails @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val getCustomer: GetCustomer,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        val TAG = "<<<<GetTxnDetails"
    }

    fun execute(req: String): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { _businessId ->
                transactionRepo.getTransaction(req, _businessId)
                    .flatMap { txn ->
                        getCustomer.execute(txn.customerId)
                            .map { Response(txn, it) }
                    }
            }
        )
    }

    data class Response(
        val transaction: merchant.okcredit.accounting.model.Transaction,
        val customer: Customer
    )
}
