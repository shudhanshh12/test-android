package `in`.okcredit.merchant.customer_ui.data.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.merchant.customer_ui.data.server.model.request.AddSubscriptionRequest
import `in`.okcredit.merchant.customer_ui.data.server.model.request.GetSubscriptionRequest
import `in`.okcredit.merchant.customer_ui.data.server.model.request.MerchantRequest
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionDetailResponse
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionListResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CustomerApiService {

    @POST("v1/CreateSubscription")
    suspend fun addSubscription(
        @Body addSubscriptionRequest: AddSubscriptionRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Subscription

    @POST("v1/UpdateSubscription")
    suspend fun updateSubscription(
        @Body deleteSubscriptionRequest: Subscription,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ResponseBody

    @POST("v1/ListSubscriptions")
    suspend fun listSubscription(
        @Body merchantRequest: MerchantRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): SubscriptionListResponse

    @POST("v1/GetSubscription")
    suspend fun getSubscription(
        @Body getSubscriptionRequest: GetSubscriptionRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): SubscriptionDetailResponse
}
