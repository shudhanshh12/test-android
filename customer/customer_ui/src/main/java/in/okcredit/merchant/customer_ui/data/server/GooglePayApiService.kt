package `in`.okcredit.merchant.customer_ui.data.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.merchant.customer_ui.data.server.model.request.GooglePayPaymentRequest
import `in`.okcredit.merchant.customer_ui.data.server.model.response.GooglePayPaymentResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GooglePayApiService {

    @POST("/v1/initiate-gpay-payment")
    suspend fun initiateGooglePayPayment(
        @Body request: GooglePayPaymentRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): GooglePayPaymentResponse
}
