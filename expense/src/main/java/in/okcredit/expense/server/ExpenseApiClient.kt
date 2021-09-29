package `in`.okcredit.expense.server

import `in`.okcredit.expense.models.Models
import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface ExpenseApiClient {

    @GET("ListExpenses")
    fun getExpenses(
        @Query("user_id") merchantId: String,
        @Query("from_date") fromDate: Long? = null,
        @Query("end_date") endDate: Long? = null,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Models.ExpenseListResponse>>

    @GET("ListUserTypes")
    fun getUserExpenseTypes(
        @Query("user_id") merchantId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Models.UserExpenseTypes>>

    @POST("AddExpense")
    fun submitExpense(
        @Body amountModel: Models.ExpenseRequestModel,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<Models.AddExpenseResponse>>

    @DELETE("DeleteExpense/{id}")
    fun deleteExpense(
        @Path("id") id: String,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Completable
}
