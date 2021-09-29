package `in`.okcredit.onboarding.enterotp.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.server.AuthApiClient
import java.util.concurrent.TimeUnit

class ResendOtpTest {

    private val authService: AuthService = mock()
    private val resendOtp = ResendOtp { authService }

    @Test
    fun `should return retry option timeout`() {
        val resendOtpResponse = AuthApiClient.ResendOtpResponse(20)
        val mobileNumber = "1234567890"
        val medium = AuthApiClient.RequestOtpMedium.SMS
        val otpId = "123456"

        whenever(authService.resendOtp(mobileNumber, medium, otpId)).thenReturn(Single.just(resendOtpResponse))

        val testObserver = resendOtp.execute(mobileNumber, medium, otpId).test().awaitDone(5, TimeUnit.SECONDS)

        testObserver.assertValue(resendOtpResponse)

        verify(authService).resendOtp(mobileNumber, medium, otpId)
    }
}
