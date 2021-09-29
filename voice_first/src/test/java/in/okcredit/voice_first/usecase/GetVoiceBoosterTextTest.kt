package `in`.okcredit.voice_first.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.voice_first.data.voice_collection.VoiceRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class GetVoiceBoosterTextTest {

    private val voiceRepository: VoiceRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getVoiceBoosterText = GetVoiceBoosterText(
        { voiceRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `execute should return a string`() {
        runBlocking {
            val text = "text"
            val businessId = "business-id"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(voiceRepository.getVoiceBoosterText(businessId)).thenReturn(text)

            Assert.assertEquals(text, getVoiceBoosterText.execute())
        }
    }
}
