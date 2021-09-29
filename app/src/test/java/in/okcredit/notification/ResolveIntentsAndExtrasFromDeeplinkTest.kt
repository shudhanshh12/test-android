package `in`.okcredit.notification

import android.content.Intent
import android.os.Bundle
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class ResolveIntentsAndExtrasFromDeeplinkTest {
    private val resolveIntentsFromDeeplink: ResolveIntentsFromDeeplink = mock()

    private val resolveIntentsAndExtrasFromDeeplinkImpl =
        ResolveIntentsAndExtrasFromDeeplinkImpl { resolveIntentsFromDeeplink }

    @Test
    fun `resolveIntentsAndExtras should call resolveIntent with expected deeplink`() {
        val fakeDeeplinkUrl = "FakeDeeplinkUrl"
        val argumentCaptorForDeeplink = argumentCaptor<String>()
        val argumentCaptorForExtras = argumentCaptor<Bundle>()
        val mockListOfIntent = listOf<Intent>(mock(), mock())

        // given
        whenever(
            resolveIntentsFromDeeplink.execute(
                argumentCaptorForDeeplink.capture(),
                argumentCaptorForExtras.capture()
            )
        ).thenReturn(mockListOfIntent)

        // when
        val result = resolveIntentsAndExtrasFromDeeplinkImpl.execute(fakeDeeplinkUrl)

        // then
        verify(resolveIntentsFromDeeplink).execute(
            argumentCaptorForDeeplink.firstValue,
            argumentCaptorForExtras.firstValue
        )

        assert(argumentCaptorForDeeplink.firstValue == fakeDeeplinkUrl)
        assert(result == mockListOfIntent)
    }

    @Test
    fun `resolveIntentsAndExtras should call resolveIntent with expected bundle`() {
        val fakeDeeplinkUrl = "FakeDeeplinkUrl"
        val argumentCaptorForString = argumentCaptor<String>()
        val argumentCaptorForExtras = argumentCaptor<Bundle>()
        val mockListOfIntent = listOf<Intent>(mock(), mock())

        // given
        whenever(
            resolveIntentsFromDeeplink.execute(
                argumentCaptorForString.capture(),
                argumentCaptorForExtras.capture()
            )
        ).thenReturn(mockListOfIntent)

        // when
        val result = resolveIntentsAndExtrasFromDeeplinkImpl.execute(fakeDeeplinkUrl)

        // then
        assert(resolveIntentsAndExtrasFromDeeplinkImpl.extras == argumentCaptorForExtras.firstValue)
        assert(argumentCaptorForString.firstValue == fakeDeeplinkUrl)
        assert(result == mockListOfIntent)
    }
}
