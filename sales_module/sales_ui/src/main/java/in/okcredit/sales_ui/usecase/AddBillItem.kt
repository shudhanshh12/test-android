package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class AddBillItem @Inject constructor(
    private val sales: SalesRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<AddBillItem.Request, BillModel.BillItemResponse> {

    data class Request(val merchantId: String = "", val addBillItemRequest: BillModel.AddBillItemRequest)

    override fun execute(req: Request): Observable<Result<BillModel.BillItemResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap {
                sales.addBillItem(req.addBillItemRequest, it)
            }
        )
    }
}
