package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.server.internal.VoiceInputResponseBody
import `in`.okcredit.backend._offline.usecase.VoiceInputSyncer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class PostVoiceInputTest {

    private val voiceInputSyncer: VoiceInputSyncer = mock()

    private val postVoiceInput = PostVoiceInput(Lazy { voiceInputSyncer })

    private val response = Response.success(VoiceInputResponseBody(1, "", "", ""))

    @Before
    fun setup() {
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `return success when api call succeeds`() {
        val dummyRequest = getDummyRequest()
        whenever(voiceInputSyncer.execute(dummyRequest.text)).thenReturn(Single.just(getDummyResponse()))
        val testObserver = postVoiceInput.execute(dummyRequest).test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(getDummyResponse())
        )
    }

    @Test
    fun `return success when api call return error`() {
        val dummyRequest = getDummyRequest()
        val mockError: Exception = mock()
        whenever(voiceInputSyncer.execute(dummyRequest.text)).thenReturn(Single.error(mockError))
        val testObserver = postVoiceInput.execute(dummyRequest).test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Failure(mockError)
        )
    }

    fun getDummyRequest(): PostVoiceInput.Request {
        return PostVoiceInput.Request("")
    }

    fun getDummyResponse(): Response<VoiceInputResponseBody> {
        return response
    }
}
