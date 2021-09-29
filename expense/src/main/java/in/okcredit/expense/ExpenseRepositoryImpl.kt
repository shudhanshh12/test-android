package `in`.okcredit.expense

import `in`.okcredit.expense.models.Models
import `in`.okcredit.expense.server.ExpenseRemoteSource
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseRemoteSource: ExpenseRemoteSource,
) : ExpenseRepository {

    override fun getExpenses(startTime: Long?, endTime: Long?, businessId: String): Single<Models.ExpenseListResponse> {
        return expenseRemoteSource.getExpenses(startTime, endTime, businessId)
    }

    override fun getUserExpenseTypes(businessId: String): Single<Models.UserExpenseTypes> {
        return expenseRemoteSource.getUserExpenseTypes(businessId)
    }

    override fun submitExpense(
        expenseRequestModel: Models.ExpenseRequestModel,
        businessId: String,
    ): Single<Models.AddExpenseResponse> {
        return expenseRemoteSource.submit(expenseRequestModel, businessId)
    }

    override fun deleteExpense(id: String, businessId: String): Completable {
        return expenseRemoteSource.deleteExpense(id, businessId)
    }
}
