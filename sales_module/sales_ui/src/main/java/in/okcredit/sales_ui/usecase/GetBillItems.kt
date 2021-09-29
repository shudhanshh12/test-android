package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetBillItems @Inject constructor(
    private val sales: SalesRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<GetBillItems.Request, BillModel.BillItemListResponse> {

    data class Request(val merchantId: String = "")

    override fun execute(req: Request): Observable<Result<BillModel.BillItemListResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap {
                sales.getBillItems(it)
            }
        )
    }
}
