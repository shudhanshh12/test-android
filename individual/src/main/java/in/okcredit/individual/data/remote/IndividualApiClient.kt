package `in`.okcredit.individual.data.remote

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface IndividualApiClient {
    @GET("v2/me")
    suspend fun getIndividual(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): GetIndividualResponse

    @POST("v1/UpdateIndividualUser")
    suspend fun updateIndividual(
        @Body request: UpdateIndividualRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    )
}
