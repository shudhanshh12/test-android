package `in`.okcredit.voice_first.data.voice_collection.server

import dagger.Lazy
import tech.okcredit.base.network.asError
import javax.inject.Inject

class VoiceRemoteDataSource @Inject constructor(
    private val voiceApiService: Lazy<VoiceApiService>,
) {
    suspend fun getBoosterVoiceText(businessId: String): String {
        val response = voiceApiService.get().getBoosterVoiceText(businessId)
        if (response.isSuccessful) {
            return response.body()?.text ?: ""
        } else {
            throw response.asError()
        }
    }

    suspend fun submitBoosterVoiceText(businessId: String) =
        voiceApiService.get().submitVoiceText(businessId).isSuccessful
}
