package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.usecase.CustomerTxnAlertDialogDismissWorker
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class SchedulerCustomerTxnALertDialogWorker @Inject constructor(
    private val dismissWorker: Lazy<CustomerTxnAlertDialogDismissWorker>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<SchedulerCustomerTxnALertDialogWorker.Request, Unit> {

    data class Request(val accountID: String)

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                dismissWorker.get().schedule(req.accountID, businessId)
            }
        )
    }
}
