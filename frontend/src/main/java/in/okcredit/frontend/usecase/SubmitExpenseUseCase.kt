package `in`.okcredit.frontend.usecase

import `in`.okcredit.expense.ExpenseRepository
import `in`.okcredit.expense.models.Models
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

class SubmitExpenseUseCase @Inject constructor(
    private val expenseRepository: Lazy<ExpenseRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<SubmitExpenseUseCase.Request, Models.AddExpenseResponse> {

    class Request(val merchantId: String, val expense: String, val expenseType: String, val expenseDate: DateTime)

    override fun execute(req: Request): Observable<Result<Models.AddExpenseResponse>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                expenseRepository.get().submitExpense(
                    Models.ExpenseRequestModel(
                        UUID.randomUUID().toString(),
                        Models.AddedExpense(
                            businessId,
                            req.expense.toDouble(),
                            req.expenseType,
                            req.expenseDate
                        )
                    ),
                    businessId
                )
            }
        )
    }
}
