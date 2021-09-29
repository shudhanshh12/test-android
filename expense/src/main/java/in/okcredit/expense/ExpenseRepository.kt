package `in`.okcredit.expense

import `in`.okcredit.expense.models.Models
import io.reactivex.Completable
import io.reactivex.Single

interface ExpenseRepository {

    fun getExpenses(
        startTime: Long? = null,
        endTime: Long? = null,
        businessId: String,
    ): Single<Models.ExpenseListResponse>

    fun getUserExpenseTypes(businessId: String): Single<Models.UserExpenseTypes>

    fun submitExpense(
        expenseRequestModel: Models.ExpenseRequestModel,
        businessId: String,
    ): Single<Models.AddExpenseResponse>

    fun deleteExpense(id: String, businessId: String): Completable
}
