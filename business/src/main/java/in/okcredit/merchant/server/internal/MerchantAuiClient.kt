package `in`.okcredit.merchant.server.internal

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MerchantAuiClient {
    @POST("v1/isActivated")
    suspend fun isMerchantActivated(
        @Body request: ApiMessages.IsMerchantActivatedApiRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.IsMerchantActivated
}
