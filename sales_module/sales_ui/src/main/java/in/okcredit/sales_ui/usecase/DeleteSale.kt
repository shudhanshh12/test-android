package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class DeleteSale @Inject constructor(
    private val salesRepository: SalesRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<DeleteSale.Request, Unit> {

    class Request(val salesId: String)

    override fun execute(req: Request): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                salesRepository.deleteSale(req.salesId, businessId)
            }
        )
    }
}
