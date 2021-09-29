package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class UpdateTransactionAmount @Inject constructor(
    val transactionRepo: TransactionRepo,
    private val coreSdk: CoreSdk,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                coreSdk.processTransactionCommand(
                    Command.UpdateTransactionAmount(req.transactionId, req.amount),
                    businessId
                ).ignoreElement()
            }
        )
    }

    data class Request(val amount: Long, val transactionId: String)
}
