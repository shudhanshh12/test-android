package `in`.okcredit.merchant.server.internal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.merchant.contract.NumberCheckResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface IdentityApiClient {
    @POST("v1/GetBusinessUser")
    fun getBusiness(
        @Body request: ApiMessages.GetBusinessRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.GetBusinessResponse>>

    @POST("/v1/CreateBusinessUser")
    suspend fun createBusiness(
        @Body request: ApiMessages.CreateBusinessRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Response<ApiMessages.GetBusinessResponse>

    @POST("/v1/UpdateBusinessUser")
    fun updateBusiness(
        @Body request: ApiMessages.UpdateBusinessRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.GetBusinessResponse>>

    @GET("v1/mobile/migrate")
    fun checkNewNumber(
        @Query("mobile") key: String?,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<NumberCheckResponse>>
}
