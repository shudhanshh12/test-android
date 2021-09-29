package tech.okcredit.android.auth

import io.reactivex.Single
import tech.okcredit.android.auth.server.AuthApiClient.FallbackOptionResponse
import tech.okcredit.android.auth.server.AuthApiClient.RequestOtpMedium
import tech.okcredit.android.auth.server.AuthApiClient.ResendOtpResponse
import tech.okcredit.android.auth.server.AuthApiClient.WhatsAppCodeRequest
import tech.okcredit.base.network.RequiresNetwork

@RequiresNetwork
interface AuthRemoteSource {
    fun requestOtp(mobile: String?): OtpToken

    fun resendOtp(
        mobileNumber: String,
        requestMedium: RequestOtpMedium,
        otpId: String,
    ): Single<ResendOtpResponse>

    fun requestFallbackOptions(mobileNumber: String): Single<FallbackOptionResponse>

    suspend fun whatsappRequestOtp(whatsAppCodeRequest: WhatsAppCodeRequest): OtpToken

    fun verifyOtp(otpId: String, code: String): String

    fun checkOtpStatus(token: OtpToken): OtpToken

    fun authenticate(credential: Credential): Grant

    fun getPasswordHash(): String?

    fun setPassword(password: String)

    fun logoutFromAllDevices(deviceId: String?)

    fun logout(deviceId: String?, token: String?)

    @Deprecated("use setPassword(password) instead, after successful authentication")
    fun resetPassword(mobile: String, verificationCode: String, newPassword: String)
}
