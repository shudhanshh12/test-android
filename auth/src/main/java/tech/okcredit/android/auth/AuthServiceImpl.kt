package tech.okcredit.android.auth

import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import org.joda.time.DateTime
import tech.okcredit.android.auth.server.AuthApiClient
import tech.okcredit.android.base.mobile.mustParseMobile
import tech.okcredit.android.base.utils.DateTimeUtils
import timber.log.Timber
import javax.inject.Inject

class AuthServiceImpl @Inject constructor(
    private val authLocalSource: Lazy<AuthLocalSource>,
    private val authRemoteSource: Lazy<AuthRemoteSource>,
    private val authInterceptor: Lazy<Interceptor>,
) : AuthService {
    override fun getCurrentMobileOtpToken(): String? {
        return authLocalSource.get().getCurrentMobileOtpToken()
    }

    override fun getNewMobileOtpToken(): String? {
        return authLocalSource.get().getNewMobileOtpToken()
    }

    override fun getPassword(): String? {
        return authLocalSource.get().getPasswordHash()
    }

    companion object {
        internal const val AUTH_HEADER = "Authorization"

        internal const val BEARER_AUTHORIZATION = "Bearer"
    }

    override fun getMobile(): String? = authLocalSource.get().getMobile()

    override fun authState(): Observable<Boolean> =
        authLocalSource.get().observeAccessToken().map { it.isNotEmpty() }.distinctUntilChanged()

    override fun isAuthenticated(): Boolean = authLocalSource.get().getAccessToken() != null

    override fun getAuthToken(): String? = authLocalSource.get().getAccessToken()

    override fun requestOtp(
        mobile: String?,
    ): OtpToken {
        var mobile_ = mobile
        if (mobile != null) {
            mobile_ = mustParseMobile(mobile)
        }
        return authRemoteSource.get().requestOtp(mobile_)
    }

    override fun resendOtp(
        mobileNumber: String,
        requestMedium: AuthApiClient.RequestOtpMedium,
        otpId: String,
    ) = authRemoteSource.get().resendOtp(mustParseMobile(mobileNumber), requestMedium, otpId)

    override fun requestFallbackOptions(mobileNumber: String) =
        authRemoteSource.get().requestFallbackOptions(mobileNumber)

    override suspend fun whatsappRequestOtp(whatsAppCodeRequest: AuthApiClient.WhatsAppCodeRequest): OtpToken {
        return authRemoteSource.get().whatsappRequestOtp(whatsAppCodeRequest)
    }

    override fun authenticate(credential: Credential): Pair<Boolean, Boolean> { // first=newUser, second=appLock
        var credential_ = credential

        // verify otp for otp credential
        if (credential is Credential.Otp && credential.token.token == null) {
            val token = verifyOtp(token = credential.token, code = credential.code)
            credential_ = credential.copy(token = credential.token.copy(token = token))
        }

        // authenticate with server
        val grant = try {
            authRemoteSource.get().authenticate(credential_)
        } catch (e: Unauthorized) {
            throw Unauthorized()
        }

        // save auth grant
        authLocalSource.get().setGrant(grant)

        // save mobile of active user
        if (grant.mobile.isNullOrBlank()) {
            throw IllegalStateException("mobile cannot be null for credential grant")
        }
        authLocalSource.get().setMobile(grant.mobile)

        return Pair(grant.newUser, grant.appLock)
    }

    override fun authenticatePhoneChangeCredential(credential: Credential): Boolean {

        // verify otp for otp credential
        if (credential is Credential.Otp && credential.token.token == null) {
            val token = verifyOtp(token = credential.token, code = credential.code)
            authLocalSource.get().setCurrentMobileOTPToken(token)
            return true
        }

        return false
    }

    override fun authenticateNewPhoneNumberCredential(credential: Credential): Boolean {

        // verify otp for otp credential
        if (credential is Credential.Otp && credential.token.token == null) {
            val token = verifyOtp(token = credential.token, code = credential.code)
            authLocalSource.get().setNewMobileOtpToken(token)
            return true
        }

        return false
    }

    override fun isPasswordSet(): Boolean {
        // check if hash is available locally
        var passwordHash = authLocalSource.get().getPasswordHash()
        if (passwordHash != null) return true

        // get hash from server
        passwordHash = authRemoteSource.get().getPasswordHash()
        if (passwordHash.isNullOrBlank()) return false

        // save hash
        authLocalSource.get().setPasswordHash(passwordHash)
        return true
    }

    override fun syncPassword() {
        val passwordHash = authRemoteSource.get().getPasswordHash()
        if (passwordHash.isNullOrBlank()) return

        // save hash
        authLocalSource.get().setPasswordHash(passwordHash)
    }

    override fun setPassword(password: String) {
        // validate password
        if (!isPasswordValid(password)) throw InvalidPassword()

        authRemoteSource.get().setPassword(password)
        authLocalSource.get().setPasswordHash(hashPassword(password))
    }

    override fun verifyPassword(password: String) {
        if (!isPasswordSet()) throw IncorrectPassword()
        if (authLocalSource.get().getPasswordHash() != hashPassword(password)) throw IncorrectPassword()
    }

    override fun logoutFromAllTheDevices(deviceId: String?) {
        authRemoteSource.get().logoutFromAllDevices(deviceId)

        // delete auth grant
        authLocalSource.get().deleteAllExceptMobile()
    }

    override fun logout(deviceId: String?, token: String?) {
        authLocalSource.get().deleteAllExceptMobile()
        // PULL Mechanism. we are not listening response.
        val subscribe = Completable.create {
            authRemoteSource.get().logout(deviceId, token)
            it.onComplete()
        }.subscribeOn(Schedulers.io())
            .subscribe(
                {
                    Timber.d("<<<<Logout Success")
                },
                { error ->
                    run {
                        Timber.d("<<<<Logout Error: %s", error.message)
                    }
                }
            )

        Timber.d("<<<<Logout Clear Success")
    }

    override fun createHttpInterceptor(): Interceptor {
        return authInterceptor.get()
    }

    private fun verifyOtp(token: OtpToken, code: String?): String {
        return if (code != null) {
            // push token received via sms
            authRemoteSource.get().verifyOtp(token.id, code)
        } else {
            // pull token sent via whatsapp, check status
            var token_ = token
            while (token_.token.isNullOrEmpty()) {
                token_ = authRemoteSource.get().checkOtpStatus(token_)
                if (token_.token.isNullOrEmpty()) {
                    Thread.sleep(200)
                }
            }
            token_.token!!
        }
    }

    suspend fun invalidateAccessToken() = authLocalSource.get().invalidateAccessToken()
}

data class Grant(
    val accessToken: String,
    val refreshToken: String,
    val expireTime: DateTime,
    val newUser: Boolean = false,
    val mobile: String? = null,
    val appLock: Boolean = false,
)

interface AccessTokenProvider {
    fun getAccessToken(forceRefresh: Boolean = false): String?
}

// utils
private fun isPasswordValid(password: String): Boolean = !password.isBlank()

internal fun Grant?.isValid(): Boolean {
    val currentTimestamp = DateTimeUtils.currentDateTime()
    return this != null && expireTime.isAfter(currentTimestamp)
}

private fun hashPassword(password: String): String {
    val payload = password + "jhgjmbsuiyoilanjjviakv"
    return BaseEncoding.base64().encode(Hashing.sha256().hashString(payload, Charsets.UTF_8).asBytes())
}
