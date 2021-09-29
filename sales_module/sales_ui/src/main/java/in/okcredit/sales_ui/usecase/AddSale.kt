package `in`.okcredit.sales_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.sales_sdk.SalesRepository
import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import javax.inject.Inject

class AddSale @Inject constructor(
    private val salesRepository: SalesRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<AddSale.Request, Models.AddSaleResponse> {

    data class Request(
        val merchantId: String,
        val amount: Double,
        val notes: String = "",
        val saleDate: DateTime = DateTime.now(),
        val buyerName: String? = null,
        val buyerMobile: String? = null,
        val billedItems: BillModel.BilledItems? = null,
    )

    override fun execute(req: Request): Observable<Result<Models.AddSaleResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute()
                .flatMap { businessId ->
                    salesRepository.submitSale(
                        Models.SaleRequestModel(
                            Models.AddSale(
                                businessId,
                                req.amount,
                                req.notes,
                                req.saleDate,
                                req.buyerName,
                                req.buyerMobile,
                                req.billedItems
                            )
                        ),
                        businessId
                    )
                }
        )
    }
}
