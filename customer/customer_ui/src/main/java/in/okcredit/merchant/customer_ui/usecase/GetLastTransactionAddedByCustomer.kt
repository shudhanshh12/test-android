package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetLastTransactionAddedByCustomer @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<String, Boolean> {

    override fun execute(req: String): Observable<Result<Boolean>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                transactionRepo.get().getLatestPaymentAddedByCustomer(req, businessId)
                    .map { it.amountV2 != 0L }
            }
        )
    }
}
