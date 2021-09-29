package `in`.okcredit.frontend.usecase
import `in`.okcredit.expense.models.Models
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import javax.inject.Inject

class FilterExpense @Inject constructor() :
    UseCase<FilterExpense.Request, Models.ExpenseListResponse> {

    data class Request(val allExpenses: List<Models.Expense>, val startDate: DateTime, val endDate: DateTime)

    override fun execute(req: Request): Observable<Result<Models.ExpenseListResponse>> {
        val filteredList = req.allExpenses.filter { expense ->
            expense.expenseDate.isAfter(req.startDate) &&
                expense.expenseDate.isBefore(req.endDate)
        }
        return UseCase.wrapSingle(
            Single.just(
                Models.ExpenseListResponse(
                    filteredList,
                    filteredList.filter { it.deletedAt == null }.sumByDouble { it.amount },
                    req.startDate,
                    req.endDate.minusDays(1)
                )
            )
        )
    }
}
