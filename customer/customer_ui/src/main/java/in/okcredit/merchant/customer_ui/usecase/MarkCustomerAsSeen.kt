package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class MarkCustomerAsSeen @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<String, Unit> {

    override fun execute(req: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                customerRepo.get().markActivityAsSeen(req, businessId)
            }
        )
    }
}
