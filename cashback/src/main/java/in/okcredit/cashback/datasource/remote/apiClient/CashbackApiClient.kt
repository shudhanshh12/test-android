package `in`.okcredit.cashback.datasource.remote.apiClient

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.merchant.rewards.server.internal.ApiMessages
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CashbackApiClient {
    @GET("message")
    fun getCashbackMessageDetails(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<CashbackMessageDetailsDto>>

    @GET("{payment_id}")
    fun getCashbackRewardForPaymentId(
        @Path("payment_id") paymentId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<ApiMessages.RewardFromApi>>
}
