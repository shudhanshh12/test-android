package `in`.okcredit.expense.server

import `in`.okcredit.expense.models.Models
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

class ExpenseRemoteSourceImpl @Inject constructor(
    private val expenseApiClient: ExpenseApiClient,
) : ExpenseRemoteSource {

    override fun getExpenses(startTime: Long?, endTime: Long?, businessId: String): Single<Models.ExpenseListResponse> {
        return expenseApiClient.getExpenses(businessId, startTime, endTime, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun getUserExpenseTypes(businessId: String): Single<Models.UserExpenseTypes> {
        return expenseApiClient.getUserExpenseTypes(businessId, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful() && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun submit(
        amountModel: Models.ExpenseRequestModel,
        businessId: String,
    ): Single<Models.AddExpenseResponse> {
        return expenseApiClient.submitExpense(amountModel, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun deleteExpense(id: String, businessId: String): Completable {
        return expenseApiClient.deleteExpense(id, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
    }
}
