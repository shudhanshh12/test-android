package `in`.okcredit.onboarding.enterotp.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.Credential
import tech.okcredit.android.auth.OtpToken

class AuthenticateOtpTest {
    private val authService: AuthService = mock()
    private val authenticateOtp = AuthenticateOtp(authService)

    @Test
    fun `should return true`() {
        val testOtpToken = OtpToken("123456", "adasdsad", "9833426881", "dwfdfwdf", "cascascsac")
        val testCredential = Credential.Otp(testOtpToken, "vsajgcvasj")

        whenever(authService.authenticatePhoneChangeCredential(testCredential))
            .thenReturn(true)

        val testObserver = authenticateOtp.execute(testCredential).test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(true)
        )
        verify(authService, times(1)).authenticatePhoneChangeCredential(testCredential)

        testObserver.dispose()
    }

    @Test
    fun `should return false`() {
        val testOtpToken = OtpToken("123456", "adasdsad", "9833426881", "dwfdfwdf", "cascascsac")
        val testCredential = Credential.Otp(testOtpToken, "vsajgcvasj")

        whenever(authService.authenticatePhoneChangeCredential(testCredential))
            .thenReturn(false)

        val testObserver = authenticateOtp.execute(testCredential).test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(false)
        )
        verify(authService, times(1)).authenticatePhoneChangeCredential(testCredential)

        testObserver.dispose()
    }
}
