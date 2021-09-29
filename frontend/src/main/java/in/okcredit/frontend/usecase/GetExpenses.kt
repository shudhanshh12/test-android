package `in`.okcredit.frontend.usecase

import `in`.okcredit.expense.ExpenseRepository
import `in`.okcredit.expense.models.Models
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

class GetExpenses @Inject constructor(
    private val expense: ExpenseRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<GetExpenses.Request, Models.ExpenseListResponse> {
    override fun execute(req: Request): Observable<Result<Models.ExpenseListResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                getExpense(req, businessId)
            }
        )
    }

    private fun getExpense(req: Request, businessId: String): Single<Models.ExpenseListResponse> {
        if (req.startDate != null && req.endDate != null) {
            return expense.getExpenses(req.startDate.millis / 1000L, req.endDate.millis / 1000L, businessId)
        }
        return expense.getExpenses(businessId = businessId)
    }

    data class Request(val merchantId: String = "", val startDate: DateTime? = null, val endDate: DateTime? = null)
}
