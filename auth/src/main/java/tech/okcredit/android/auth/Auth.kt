package tech.okcredit.android.auth

import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.Interceptor
import tech.okcredit.android.auth.server.AuthApiClient.FallbackOptionResponse
import tech.okcredit.android.auth.server.AuthApiClient.RequestOtpMedium
import tech.okcredit.android.auth.server.AuthApiClient.ResendOtpResponse
import tech.okcredit.android.auth.server.AuthApiClient.WhatsAppCodeRequest
import tech.okcredit.android.base.mobile.InvalidMobile
import tech.okcredit.base.network.RequiresNetwork

// AuthService handles all authentication related stuff
interface AuthService {

    // get mobile of authenticated merchant, return null if no authenticated merchant
    fun getMobile(): String?

    fun isAuthenticated(): Boolean

    fun getAuthToken(): String?

    fun authState(): Observable<Boolean>

    fun getCurrentMobileOtpToken(): String?

    fun getNewMobileOtpToken(): String?

    fun getPassword(): String?

    @RequiresNetwork
    @Throws(InvalidMobile::class)
    fun requestOtp(mobile: String? = null): OtpToken

    @RequiresNetwork
    fun resendOtp(
        mobileNumber: String,
        requestMedium: RequestOtpMedium,
        otpId: String,
    ): Single<ResendOtpResponse>

    @RequiresNetwork
    fun requestFallbackOptions(mobileNumber: String): Single<FallbackOptionResponse>

    @RequiresNetwork
    @Throws(InvalidMobile::class)
    suspend fun whatsappRequestOtp(whatsAppCodeRequest: WhatsAppCodeRequest): OtpToken

    // returns true on new registration
    @RequiresNetwork
    @Throws(Unauthorized::class, InvalidOtp::class, ExpiredOtp::class)
    fun authenticate(credential: Credential): Pair<Boolean, Boolean>

    @RequiresNetwork
    @Throws(Unauthorized::class, InvalidOtp::class, ExpiredOtp::class)
    fun authenticatePhoneChangeCredential(credential: Credential): Boolean

    @RequiresNetwork
    @Throws(Unauthorized::class, InvalidOtp::class, ExpiredOtp::class)
    fun authenticateNewPhoneNumberCredential(req: Credential): Boolean

    @RequiresNetwork
    @Throws(Unauthorized::class)
    fun isPasswordSet(): Boolean

    @RequiresNetwork
    @Throws(Unauthorized::class)
    fun syncPassword()

    @RequiresNetwork
    @Throws(InvalidPassword::class, Unauthorized::class)
    fun setPassword(password: String)

    @Throws(Unauthorized::class, IncorrectPassword::class)
    fun verifyPassword(password: String)

    @RequiresNetwork
    @Throws(Unauthorized::class)
    fun logoutFromAllTheDevices(deviceId: String?)

    @RequiresNetwork
    @Throws(Unauthorized::class)
    fun logout(deviceId: String?, token: String?)

    // OkHttp interceptor which injects authorization header into requests
    fun createHttpInterceptor(): Interceptor
}

sealed class Credential {
    data class Password(val mobile: String, val password: String) : Credential()
    data class Otp(val token: OtpToken, val code: String? = null) : Credential()
    data class Truecaller(val payload: String, val signature: String) : Credential()
}

data class OtpToken(
    val id: String,
    internal val key: String? = null,
    internal val mobile: String? = null,
    internal val otp: String? = null,
    internal val token: String? = null,
    val overallExpiryTime: Int? = null,
    val fallbackOptionsShowTime: Int? = null,
) {
    fun encode(): String {
        return "code: $otp"
    }
}
