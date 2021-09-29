package `in`.okcredit.frontend.usecase

import `in`.okcredit.expense.ExpenseRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class DeleteExpense @Inject constructor(
    private val expense: Lazy<ExpenseRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(id: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                expense.get().deleteExpense(id, businessId)
            }
        )
    }
}
