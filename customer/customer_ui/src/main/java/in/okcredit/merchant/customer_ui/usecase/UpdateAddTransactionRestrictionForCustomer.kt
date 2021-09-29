package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable

class UpdateAddTransactionRestrictionForCustomer @javax.inject.Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<UpdateAddTransactionRestrictionForCustomer.Request, Unit> {

    data class Request(val accountID: String)

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                customerRepo.get().updateAddTransactionRestrictedLocally(req.accountID, businessId)
            }
        )
    }
}
