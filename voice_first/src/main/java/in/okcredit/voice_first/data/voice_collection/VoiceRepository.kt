package `in`.okcredit.voice_first.data.voice_collection

import `in`.okcredit.voice_first.data.voice_collection.server.VoiceRemoteDataSource
import javax.inject.Inject

class VoiceRepository @Inject constructor(
    private val voiceRemoteDataSource: VoiceRemoteDataSource,
) {
    suspend fun getVoiceBoosterText(businessId: String): String = voiceRemoteDataSource.getBoosterVoiceText(businessId)

    suspend fun submitBoosterVoiceText(businessId: String): Boolean =
        voiceRemoteDataSource.submitBoosterVoiceText(businessId)
}
