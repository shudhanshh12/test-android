package `in`.okcredit.voice_first.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.voice_first.data.voice_collection.VoiceRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SubmitVoiceBoosterTextTest {

    private val voiceRepository: VoiceRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val submitVoiceBoosterText = SubmitVoiceBoosterText(
        { voiceRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `execute should return true if success`() {
        runBlocking {
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(voiceRepository.submitBoosterVoiceText(businessId)).thenReturn(true)

            assertEquals(true, submitVoiceBoosterText.execute())
        }
    }

    @Test
    fun `execute should return false if fails`() {
        runBlocking {
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(voiceRepository.submitBoosterVoiceText(businessId)).thenReturn(false)

            assertEquals(false, submitVoiceBoosterText.execute())
        }
    }
}
