package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import javax.inject.Inject

class GetCashSales @Inject constructor(
    private val sales: SalesRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<GetCashSales.Request, Models.SalesListResponse> {

    data class Request(val merchantId: String = "", val startDate: DateTime? = null, val endDate: DateTime? = null)

    override fun execute(req: Request): Observable<Result<Models.SalesListResponse>> {
        if (req.startDate != null && req.endDate != null) {
            return UseCase.wrapSingle(
                getActiveBusinessId.get().execute().flatMap { businessId ->
                    sales.getSales(req.startDate.millis / 1000L, req.endDate.millis / 1000L, businessId)
                }
            )
        }
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                sales.getSales(businessId = businessId)
            }
        )
    }
}
