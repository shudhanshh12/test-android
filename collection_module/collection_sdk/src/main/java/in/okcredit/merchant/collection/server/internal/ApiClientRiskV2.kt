package `in`.okcredit.merchant.collection.server.internal

import `in`.okcredit.collection.contract.ApiMessages
import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiClientRiskV2 {
    @POST("GetRiskAttributes")
    suspend fun getRiskAttributes(
        @Body request: ApiMessages.GetRiskAttributesRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): ApiMessages.GetRiskAttributesResponse
}
