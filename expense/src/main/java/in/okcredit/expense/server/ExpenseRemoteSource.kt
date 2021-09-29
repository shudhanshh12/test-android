package `in`.okcredit.expense.server

import `in`.okcredit.expense.models.Models
import io.reactivex.Completable
import io.reactivex.Single

interface ExpenseRemoteSource {

    fun getExpenses(
        startTime: Long? = null,
        endTime: Long? = null,
        businessId: String,
    ): Single<Models.ExpenseListResponse>

    fun getUserExpenseTypes(businessId: String): Single<Models.UserExpenseTypes>

    fun submit(amountModel: Models.ExpenseRequestModel, businessId: String): Single<Models.AddExpenseResponse>

    fun deleteExpense(id: String, businessId: String): Completable
}
