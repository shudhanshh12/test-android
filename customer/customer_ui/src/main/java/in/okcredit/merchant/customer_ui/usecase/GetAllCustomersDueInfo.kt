package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.DueInfoRepo
import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetAllCustomersDueInfo<Request, Response> @Inject constructor(
    private val allDueInfoRepo: DueInfoRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<GetAllCustomersDueInfo.Request, GetAllCustomersDueInfo.Response> {
    override fun execute(req: GetAllCustomersDueInfo.Request): Observable<Result<GetAllCustomersDueInfo.Response>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                Observable.just(Response(allDueInfoRepo.getAllCustomerDueInfo(businessId)))
            }
        )
    }

    class Request

    data class Response(val allCustomerDueInfo: Observable<MutableList<DueInfo>>)
}
