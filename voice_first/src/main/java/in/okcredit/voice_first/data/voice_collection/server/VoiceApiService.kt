package `in`.okcredit.voice_first.data.voice_collection.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.voice_first.data.voice_collection.server.response.VoiceBoosterResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface VoiceApiService {
    @GET("v1/voice-collection/fetch")
    suspend fun getBoosterVoiceText(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Response<VoiceBoosterResponse>

    @POST("v1/voice-collection/submit")
    suspend fun submitVoiceText(
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Response<Unit>
}
