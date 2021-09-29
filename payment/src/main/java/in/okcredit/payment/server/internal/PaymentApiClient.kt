package `in`.okcredit.payment.server.internal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface PaymentApiClient {

    @POST("payment_link/{link_id}/attributes")
    fun getPaymentAttributes(
        @Body request: PaymentApiMessages.PaymentAttributeRequestBody,
        @Path("link_id") link_id: String,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<PaymentApiMessages.GetPaymentAttributesResponse>>

    @POST("juspay/attributes")
    fun getJuspayAttributes(
        @Body request: PaymentApiMessages.JuspayAttributeRequestBody,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<PaymentApiMessages.GetJuspayAttributesResponse>>

    @GET("payment/{payment_id}")
    fun juspayPaymentPolling(
        @Path("payment_id") payment_id: String,
        @Query("polling") polling: Boolean,
        @Query("type") type: String,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<PaymentApiMessages.JuspayPaymentPollingResponse>>

    @PUT("destination/{merchant_id}")
    fun createPaymentDestination(
        @Path("merchant_id") merchantId: String,
        @Body request: PaymentApiMessages.PaymentDestinationRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<PaymentApiMessages.PaymentDestinationResponse>>
}

enum class PaymentDestinationType(val value: String) {
    UPI("upi"),
    PAY_TM("paytm"),
    BANK("bank")
}

enum class JuspayEventType(val value: String) {
    INITIATE("INITIATE"),
    PROCESS("PROCESS")
}
