package tech.okcredit.web.utils

import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class WebViewUtilsTest {

    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()

    @Before
    fun setup() {
        mockkStatic(FirebaseRemoteConfig::class)
        every { FirebaseRemoteConfig.getInstance() } returns firebaseRemoteConfig
    }

    @Test
    fun `when both uris are null returns false`() {
        val result = WebViewUtils.isAcceptableFailure(null, null)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when okcredit domain is passed and whitelisted domains are fetched from remote config`() {
        whenever(firebaseRemoteConfig.getString("whitelisted_domains")).thenReturn("okcredit.in,okrelief.in,okshop.in")

        val url = "https://www.okcredit.in"
        val result = url.isThirdPartyUrl()

        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when non okcredit domain is passed and whitelisted domains are fetched from remote config`() {
        whenever(firebaseRemoteConfig.getString("whitelisted_domains")).thenReturn("okcredit.in,okrelief.in,okshop.in")

        val url = "https://www.google.com"
        val result = url.isThirdPartyUrl()

        assertThat(result).isTrue()
    }

    @Test
    fun `should return true when okcredit domain is passed and whitelisted domains are not fetched from remote config`() {
        whenever(firebaseRemoteConfig.getString("whitelisted_domains")).thenReturn("")

        val url = "https://www.okcredit.in"
        val result = url.isThirdPartyUrl()

        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when non okcredit domain is passed and whitelisted domains are not fetched from remote config`() {
        whenever(firebaseRemoteConfig.getString("whitelisted_domains")).thenReturn("")

        val url = "https://www.google.com"
        val result = url.isThirdPartyUrl()

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when test WebView url is passed`() {
        val url = "https://okcredit-42.firebaseapp.com"
        val result = url.isThirdPartyUrl()

        assertThat(result).isFalse()
    }
}
