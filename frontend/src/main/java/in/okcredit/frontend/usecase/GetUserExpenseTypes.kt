package `in`.okcredit.frontend.usecase

import `in`.okcredit.expense.ExpenseRepository
import `in`.okcredit.expense.models.Models
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetUserExpenseTypes @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<GetUserExpenseTypes.Request, Models.UserExpenseTypes> {

    class Request(val merchantId: String)

    override fun execute(req: Request): Observable<Result<Models.UserExpenseTypes>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                expenseRepository.getUserExpenseTypes(businessId)
            }
        )
    }
}
