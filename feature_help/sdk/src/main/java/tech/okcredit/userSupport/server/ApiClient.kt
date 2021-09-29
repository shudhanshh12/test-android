package tech.okcredit.userSupport.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import tech.okcredit.userSupport.model.UserSuccessFeedBackRequest

interface ApiClient {

    @GET("help")
    fun getHelp(
        @Query("merchant_id") merchant_id: String,
        @Query("lang") language: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<HelpApiResponse>

    @POST("feedback")
    fun submitFeedback(
        @Body userSuccessFeedBackRequest: UserSuccessFeedBackRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable
}
