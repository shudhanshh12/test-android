package `in`.okcredit.voice_first.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.voice_first.data.voice_collection.VoiceRepository
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class SubmitVoiceBoosterText @Inject constructor(
    private val voiceRepository: Lazy<VoiceRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    suspend fun execute(): Boolean {
        val businessId = getActiveBusinessId.get().execute().await()
        return voiceRepository.get().submitBoosterVoiceText(businessId)
    }
}
