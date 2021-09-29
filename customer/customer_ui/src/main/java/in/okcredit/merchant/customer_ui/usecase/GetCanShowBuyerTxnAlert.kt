package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCanShowBuyerTxnAlert @Inject constructor(
    val customerRepo: Lazy<CustomerRepo>,
    val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<GetCanShowBuyerTxnAlert.Request, Boolean> {

    class Request(val accountId: String)

    override fun execute(req: Request): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                customerRepo.get().allCustomersBuyerTxnAlertFeatureList(businessId).map {
                    it.getOrElse(req.accountId, { false })
                }.distinctUntilChanged { t1: Boolean, t2: Boolean -> t1 == t2 }
            }
        )
    }
}
