package `in`.okcredit.onboarding.enterotp.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.OtpToken
import java.util.concurrent.TimeUnit

class RequestOtpTest {
    private val authService: AuthService = mock()
    private val requestOtp = RequestOtp(authService)

    @Test
    fun `should give success OtpToken for mobile passed`() {
        val testMobile = "9833426881"
        val testOtpToken = OtpToken("123456", "adasdsad", "9833426881", "dwfdfwdf", "cascascsac")

        whenever(authService.requestOtp(testMobile))
            .thenReturn(testOtpToken)

        val testObserver = requestOtp.execute(testMobile).test().awaitDone(5, TimeUnit.SECONDS)
        testObserver.assertValue(testOtpToken)
        verify(authService, times(1)).requestOtp(testMobile)

        testObserver.dispose()
    }

    @Test
    fun `should give success OtpToken for no mobile passed`() {
        val testMobile = null
        val testOtpToken = OtpToken("123456", "adasdsad", "9833426881", "dwfdfwdf", "cascascsac")

        whenever(authService.requestOtp())
            .thenReturn(testOtpToken)

        val testObserver = requestOtp.execute(testMobile).test().awaitDone(5, TimeUnit.SECONDS)
        testObserver.assertValue(testOtpToken)
        verify(authService, times(1)).requestOtp()

        testObserver.dispose()
    }
}
