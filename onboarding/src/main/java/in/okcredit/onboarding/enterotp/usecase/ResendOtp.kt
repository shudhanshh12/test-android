package `in`.okcredit.onboarding.enterotp.usecase

import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.auth.server.AuthApiClient
import javax.inject.Inject

class ResendOtp @Inject constructor(
    private val authService: Lazy<AuthService>,
) {
    fun execute(
        mobileNumber: String,
        requestMedium: AuthApiClient.RequestOtpMedium,
        otpId: String,
    ): Single<AuthApiClient.ResendOtpResponse> {
        return authService.get().resendOtp(mobileNumber, requestMedium, otpId)
    }
}
