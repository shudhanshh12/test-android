package tech.okcredit.android.auth.server

import androidx.annotation.Keep
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import tech.okcredit.android.auth.AuthServiceImpl

// api spec
interface AuthApiClient {

    interface Protected {
        @POST("v1.0/logout")
        fun logout(@Body request: SignOutRequest): Call<Unit>

        @GET("v1.0/password")
        fun checkPasswordSet(): Call<CheckPasswordSetResponse>

        @POST("v1.0/password")
        fun setPassword(@Body req: SetPasswordRequest): Call<Unit>
    }

    @POST("v1.0/logout")
    fun logout(
        @Body request: SignOutRequest,
        @Header(AuthServiceImpl.AUTH_HEADER) accessToken: String,
    ): Call<Unit>

    @POST("v2/auth")
    fun authenticate(@Body req: AuthenticateRequest): Call<AuthenticateResponse>

    @POST("v1.0/password")
    fun resetPassword(@Body req: ResetPasswordRequest): Call<Unit>

    @POST("v1.0/otp:request")
    fun requestOtp(@Body req: RequestOtpRequest): Call<RequestOtpResponse>

    @POST("v1.0/otp/retry")
    fun resendOtp(@Body req: ResendOtpRequest): Single<Response<ResendOtpResponse>>

    @POST("v1.0/otp/retry/options")
    fun requestFallbackOptions(@Body req: FallbackOptionRequest): Single<Response<FallbackOptionResponse>>

    @POST("v1.0/otp/whatsapp/code")
    suspend fun requestWhatsappCode(@Body req: WhatsAppCodeRequest): WhatsAppCodeResponse

    @POST("v1.0/otp:verify")
    fun verifyOtp(@Body req: VerifyOtpRequest): Call<VerifyOtpResponse>

    @GET("v1.0/otp/{otp_id}/status")
    fun checkOtpStatus(@Path("otp_id") otp_id: String, @Query("otp_key") otp_key: String): Call<CheckOtpStatusResponse>

    companion object {
        const val GRANT_TYPE_PASSWORD = "password"
        const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"
        const val GRANT_TYPE_OTP = "otp"
        const val GRANT_TYPE_TRUECALLER = "truecaller"

        const val OTP_MODE_PUSH = "PUSH"
        const val OTP_MODE_PULL = "PULL"

        // by default it is kept as 10 seconds
        const val OTP_RETRY_TIME = 10

        // process of entering OTP should reset after 300 seconds, if the user is on the OTP screen
        const val OTP_FLOW_EXPIRY_TIME = 300
    }

    @Keep
    data class AuthenticateRequest(
        val grant_type: String,
        val username: String? = null,
        val password: String? = null,
        val refresh_token: String? = null,
        val assertion: String? = null,
        val origin: Int = 0, // 0 = OkCredit
    )

    @Keep
    data class AuthenticateResponse(
        val access_token: String,
        val refresh_token: String,
        val expires_in: Int,
        val new_user: Boolean = false,
        val mobile: String? = null,
        val app_lock: Boolean = false,
    )

    @Keep
    data class ResetPasswordRequest(
        val mobile: String,
        val verification_method: String = "otp",
        val verification_token: String,
        val password: String,
    )

    @Keep
    data class RequestOtpRequest(
        val mode: String,
        val mobile: String?,
    )

    @Keep
    data class RequestOtpResponse(
        val otp_id: String,
        val otp: String?,
        val otp_key: String?,
        val otp_flow_timeout: Int?,
        val fallback_options_show_time: Int? = OTP_RETRY_TIME
    )

    @Keep
    data class VerifyOtpRequest(
        val mode: String = OTP_MODE_PUSH,
        val otp_id: String,
        val otp: String,
    )

    @Keep
    data class VerifyOtpResponse(
        val token: String,
    )

    @Keep
    data class CheckOtpStatusResponse(
        val status: Int,
        val token: String?,
        val mobile: String?,
    )

    @Keep
    data class SetPasswordRequest(
        val password: String,
    )

    @Keep
    data class CheckPasswordSetResponse(
        val password_set: Boolean,
        val checksum: String?,
    )

    @Keep
    data class SignOutRequest(
        val type: Int,
        val device_id: String?,
    )

    @Keep
    data class WhatsAppCodeRequest(
        val distinct_id: String?,
        val redirect_url: String,
        val lang: String,
        val mobile: String,
        val purpose: String,
    )

    @Keep
    data class WhatsAppCodeResponse(
        val otp_id: String,
        val otp_key: String,
        val otp_code: String,
    )

    @Keep
    data class ResendOtpRequest(
        val intent: Int,
        val otp_id: String,
        val mobile: String,
        val language: String,
        val destination: Int = RetryDestination.PRIMARY.key,
        val mixpanel_distinct_id: String,
        // {0 for OkCredit, 1 for OkStaff}
        val request_origin: Int = 0,
    )

    @Keep
    data class ResendOtpResponse(
        val retry_option_timeout: Int?,
    )

    @Keep
    data class FallbackOptionRequest(
        val mixpanel_distinct_id: String,
        val mobile: String,
        val language: String,
    )

    @Keep
    data class FallbackOptionResponse(
        val retry_options: ArrayList<FallbackOption>,
    )

    @Keep
    data class FallbackOption(
        // It will be one of the values from RetryDestination
        val destination: Int,
        // It will consist of the values from RequestOtpMedium
        val intents: ArrayList<Int>,
    )

    @Keep
    enum class RetryDestination(val key: Int) {
        PRIMARY(0),
        SECONDARY(1);

        companion object {
            fun getDestination(code: Int?) = when (code) {
                SECONDARY.key -> SECONDARY
                else -> PRIMARY
            }
        }
    }

    @Keep
    enum class RequestOtpMedium(val key: Int) {
        SMS(0),
        WHATSAPP(1),
        CALL(2);

        companion object {
            fun getMedium(code: Int?) = when (code) {
                WHATSAPP.key -> WHATSAPP
                CALL.key -> CALL
                else -> SMS
            }
        }
    }
}
