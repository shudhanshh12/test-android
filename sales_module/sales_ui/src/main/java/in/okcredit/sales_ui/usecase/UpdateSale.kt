package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class UpdateSale @Inject constructor(
    private val sales: SalesRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<UpdateSale.Request, Models.SaleItemResponse> {

    data class Request(val saleId: String, val updateSaleItemRequest: Models.UpdateSaleItemRequest)

    override fun execute(req: Request): Observable<Result<Models.SaleItemResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                sales.updateSale(req.saleId, req.updateSaleItemRequest, businessId)
            }
        )
    }
}
