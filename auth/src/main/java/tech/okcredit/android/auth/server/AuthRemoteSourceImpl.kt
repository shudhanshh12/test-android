package tech.okcredit.android.auth.server

import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.auth.*
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.mobile.InvalidMobile
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

class AuthRemoteSourceImpl @Inject constructor(
    private val authApiClient: Lazy<AuthApiClient>,
    private val protectedAuthApiClient: Lazy<AuthApiClient.Protected>,
    private val localeManager: Lazy<LocaleManager>,
    private val mixpanelAPI: Lazy<MixpanelAPI>,
) : AuthRemoteSource {

    companion object {
        const val TOO_MANY_REQUESTS_ERROR_CODE = 429
        const val INVALID_MODE_ERROR = "okcredit.auth.invalid_mobile"
        const val INVALID_PASSWORD_ERROR = "okcredit.auth.invalid_password"
        const val INVALID_VERIFICATION_TOKEN_ERROR = "okcredit.auth.invalid_verification_token"
        const val INVALID_OTP_ERROR = "invalid_otp"
        const val OTP_EXPIRED_ERROR = "otp_expired"
    }

    override fun authenticate(credential: Credential): Grant = authenticate(
        when (credential) {
            is Credential.Password -> AuthApiClient.AuthenticateRequest(
                grant_type = AuthApiClient.GRANT_TYPE_PASSWORD,
                username = credential.mobile,
                password = credential.password
            )

            is Credential.Otp -> AuthApiClient.AuthenticateRequest(
                grant_type = AuthApiClient.GRANT_TYPE_OTP,
                assertion = credential.token.token
            )

            is Credential.Truecaller -> AuthApiClient.AuthenticateRequest(
                grant_type = AuthApiClient.GRANT_TYPE_TRUECALLER,
                assertion = "${credential.payload}|${credential.signature}"
            )
        }
    )

    override fun logout(deviceId: String?, token: String?) {
        val res = authApiClient.get().logout(
            AuthApiClient.SignOutRequest(
                type = 1,
                device_id = deviceId
            ),
            "${AuthServiceImpl.BEARER_AUTHORIZATION} $token"
        ).execute()

        if (!res.isSuccessful) throw res.asError()
    }

    override fun logoutFromAllDevices(deviceId: String?) {
        val res = protectedAuthApiClient.get().logout(
            AuthApiClient.SignOutRequest(
                type = 0,
                device_id = deviceId
            )
        ).execute()

        if (!res.isSuccessful) throw res.asError()
    }

    override fun resetPassword(mobile: String, verificationCode: String, newPassword: String) {

        val res = authApiClient.get().resetPassword(
            AuthApiClient.ResetPasswordRequest(
                mobile = mobile,
                verification_token = verificationCode,
                password = newPassword
            )
        ).execute()

        if (!res.isSuccessful) {
            throw res.asError().mapError(
                INVALID_MODE_ERROR to InvalidMobile(),
                INVALID_PASSWORD_ERROR to InvalidPassword(),
                INVALID_VERIFICATION_TOKEN_ERROR to Unauthorized()
            )
        }
    }

    override fun requestOtp(mobile: String?): OtpToken {
        var mode = AuthApiClient.OTP_MODE_PUSH
        if (mobile == null) {
            mode = AuthApiClient.OTP_MODE_PULL
        }

        val res = authApiClient.get().requestOtp(
            AuthApiClient.RequestOtpRequest(
                mode = mode,
                mobile = mobile
            )
        ).execute()

        if (res.isSuccessful) {
            return res.body()!!.toOtp(mobile)
        } else {
            throw res.asError().mapError(
                INVALID_MODE_ERROR to InvalidMobile()
            )
        }
    }

    override fun resendOtp(
        mobileNumber: String,
        requestMedium: AuthApiClient.RequestOtpMedium,
        otpId: String,
    ): Single<AuthApiClient.ResendOtpResponse> {
        return authApiClient.get().resendOtp(
            AuthApiClient.ResendOtpRequest(
                mobile = mobileNumber,
                intent = requestMedium.key,
                otp_id = otpId,
                language = localeManager.get().getLanguage(),
                mixpanel_distinct_id = mixpanelAPI.get().distinctId
            )
        ).subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful) {
                    return@map res.body()!!
                } else {
                    when (res.code()) {
                        TOO_MANY_REQUESTS_ERROR_CODE -> {
                            throw res.asError().mapCode(
                                TOO_MANY_REQUESTS_ERROR_CODE to TooManyRequests()
                            )
                        }
                        else -> {
                            throw res.asError().mapError(
                                INVALID_MODE_ERROR to InvalidMobile()
                            )
                        }
                    }
                }
            }
    }

    override fun requestFallbackOptions(mobileNumber: String): Single<AuthApiClient.FallbackOptionResponse> {
        return authApiClient.get().requestFallbackOptions(
            AuthApiClient.FallbackOptionRequest(
                mobile = mobileNumber,
                language = localeManager.get().getLanguage(),
                mixpanel_distinct_id = mixpanelAPI.get().distinctId
            )
        )
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()
                } else {
                    throw it.asError()
                }
            }
    }

    override suspend fun whatsappRequestOtp(whatsAppCodeRequest: AuthApiClient.WhatsAppCodeRequest): OtpToken {
        return authApiClient.get().requestWhatsappCode(whatsAppCodeRequest).let {
            OtpToken(
                id = it.otp_id,
                key = it.otp_key,
                otp = it.otp_code,
            )
        }
    }

    override fun verifyOtp(otpId: String, code: String): String {
        val res = authApiClient.get().verifyOtp(
            AuthApiClient.VerifyOtpRequest(
                otp_id = otpId,
                otp = code
            )
        ).execute()

        if (res.isSuccessful) {
            return res.body()!!.token
        } else {
            when (res.code()) {
                TOO_MANY_REQUESTS_ERROR_CODE -> {
                    throw res.asError().mapCode(
                        TOO_MANY_REQUESTS_ERROR_CODE to TooManyRequests()
                    )
                }
                else -> {
                    throw res.asError().mapError(
                        INVALID_OTP_ERROR to InvalidOtp(),
                        OTP_EXPIRED_ERROR to ExpiredOtp()
                    )
                }
            }
        }
    }

    override fun checkOtpStatus(token: OtpToken): OtpToken {
        val res = authApiClient.get().checkOtpStatus(token.id, token.key!!).execute()

        if (res.isSuccessful) {
            return res.body()!!.insertInto(token)
        } else {
            throw res.asError().mapError(
                INVALID_OTP_ERROR to InvalidOtp()
            )
        }
    }

    override fun getPasswordHash(): String? {
        val res = protectedAuthApiClient.get().checkPasswordSet().execute()

        if (res.isSuccessful) {
            if (res.body()!!.password_set) {
                return res.body()!!.checksum
            } else {
                return null
            }
        } else {
            throw res.asError()
        }
    }

    override fun setPassword(password: String) {
        val res = protectedAuthApiClient.get().setPassword(AuthApiClient.SetPasswordRequest(password)).execute()
        if (!res.isSuccessful) throw res.asError().mapError(INVALID_PASSWORD_ERROR to InvalidPassword())
    }

    private fun authenticate(req: AuthApiClient.AuthenticateRequest): Grant {
        val res = authApiClient.get().authenticate(req).execute()
        if (res.isSuccessful) return res.body()!!.toGrant()
        else throw res.asError().mapCode(400 to Unauthorized())
    }
}

// api response mappers
internal fun AuthApiClient.AuthenticateResponse.toGrant(): Grant = Grant(
    accessToken = access_token,
    refreshToken = refresh_token,
    expireTime = DateTimeUtils.currentDateTime().plusSeconds(expires_in).minusMinutes(1),
    newUser = new_user,
    mobile = mobile,
    appLock = app_lock
)

internal fun AuthApiClient.RequestOtpResponse.toOtp(mobile: String?): OtpToken = OtpToken(
    id = otp_id,
    otp = otp,
    key = otp_key,
    mobile = mobile,
    overallExpiryTime = otp_flow_timeout,
    fallbackOptionsShowTime = fallback_options_show_time
)

internal fun AuthApiClient.CheckOtpStatusResponse.insertInto(token: OtpToken): OtpToken = token.copy(
    token = this.token,
    mobile = mobile
)
