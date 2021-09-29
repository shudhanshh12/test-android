package `in`.okcredit.onboarding.enterotp.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.Credential
import tech.okcredit.android.auth.OtpToken

class AuthenticateNewOtpTest {
    private val authService: AuthService = mock()
    private val authenticateNewOtp = AuthenticateNewOtp(authService)

    @Test
    fun `should return true`() {
        val testOtpToken = OtpToken("123456", "adasdsad", "9833426881", "dwfdfwdf", "cascascsac")
        val testCredential = Credential.Otp(testOtpToken, "vsajgcvasj")

        whenever(authService.authenticateNewPhoneNumberCredential(testCredential))
            .thenReturn(true)

        val testObserver = authenticateNewOtp.execute(testCredential).test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(true)
        )

        verify(authService, times(1)).authenticateNewPhoneNumberCredential(testCredential)
        testObserver.dispose()
    }

    @Test
    fun `should return false`() {
        val testOtpToken = OtpToken("123456", "adasdsad", "9833426881", "dwfdfwdf", "cascascsac")
        val testCredential = Credential.Otp(testOtpToken, "vsajgcvasj")

        whenever(authService.authenticateNewPhoneNumberCredential(testCredential))
            .thenReturn(false)

        val testObserver = authenticateNewOtp.execute(testCredential).test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(false)
        )
        verify(authService, times(1)).authenticateNewPhoneNumberCredential(testCredential)

        testObserver.dispose()
    }
}
