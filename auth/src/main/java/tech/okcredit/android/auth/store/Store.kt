package tech.okcredit.android.auth.store

import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.asObservable
import org.joda.time.DateTime
import tech.okcredit.android.auth.AuthLocalSource
import tech.okcredit.android.auth.Grant
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.blockingGetLong
import tech.okcredit.android.base.preferences.blockingGetString
import tech.okcredit.android.base.preferences.blockingSet
import tech.okcredit.android.base.utils.ThreadUtils
import javax.inject.Inject

class AuthLocalSourceImpl @Inject constructor(
    private val prefs: Lazy<AuthPreferences>,
) : AuthLocalSource {
    override fun setCurrentMobileOTPToken(token: String) {
        prefs.get().blockingSet(CURRENT_MOBILE_OTP_TOKEN, token, Scope.Individual)
    }

    override fun setNewMobileOtpToken(token: String) {
        prefs.get().blockingSet(NEW_MOBILE_OTP_TOKEN, token, Scope.Individual)
    }

    override fun getNewMobileOtpToken(): String? {
        return prefs.get().blockingGetString(NEW_MOBILE_OTP_TOKEN, Scope.Individual, null)
    }

    override fun getCurrentMobileOtpToken(): String? {
        return prefs.get().blockingGetString(CURRENT_MOBILE_OTP_TOKEN, Scope.Individual, null)
    }

    companion object {
        internal const val KEY_MOBILE = "mobile"
        internal const val KEY_ACCESS_TOKEN = "access_token"
        internal const val KEY_REFRESH_TOKEN = "refresh_token"
        internal const val KEY_EXPIRE_TIME = "expire_time"
        internal const val KEY_PASSWORD_HASH = "password_hash_"
        internal const val CURRENT_MOBILE_OTP_TOKEN = "current_mobile_otp_token"
        internal const val NEW_MOBILE_OTP_TOKEN = "new_mobile_otp_token"
    }

    override fun observeAccessToken(): Observable<String> =
        prefs.get().getString(KEY_ACCESS_TOKEN, Scope.Individual).asObservable().subscribeOn(ThreadUtils.database())

    override fun getAccessToken(): String? = prefs.get().blockingGetString(KEY_ACCESS_TOKEN, Scope.Individual, null)

    override fun getGrant(): Grant? = prefs.get().run {
        val accessToken = blockingGetString(KEY_ACCESS_TOKEN, Scope.Individual, null) ?: return null
        val refreshToken = blockingGetString(KEY_REFRESH_TOKEN, Scope.Individual, null) ?: return null
        val expireTime = DateTime(blockingGetLong(KEY_EXPIRE_TIME, Scope.Individual, 0))
        return Grant(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expireTime = expireTime
        )
    }

    override fun setGrant(grant: Grant) {
        prefs.get().blockingSet(KEY_ACCESS_TOKEN, grant.accessToken, Scope.Individual)
        prefs.get().blockingSet(KEY_REFRESH_TOKEN, grant.refreshToken, Scope.Individual)
        prefs.get().blockingSet(KEY_EXPIRE_TIME, grant.expireTime.millis, Scope.Individual)
    }

    override fun deleteAllExceptMobile() = runBlocking {
        val mobile = prefs.get().getString(KEY_MOBILE, Scope.Individual).first()
        prefs.get().clear()
        prefs.get().set(KEY_MOBILE, mobile, Scope.Individual)
    }

    override fun getMobile(): String? = prefs.get().blockingGetString(KEY_MOBILE, Scope.Individual, null)

    override fun setMobile(mobile: String) {
        prefs.get().blockingSet(KEY_MOBILE, mobile, Scope.Individual)
    }

    override fun getPasswordHash(): String? = prefs.get().blockingGetString(KEY_PASSWORD_HASH, Scope.Individual, null)

    override fun setPasswordHash(passwordHash: String) {
        prefs.get().blockingSet(KEY_PASSWORD_HASH, passwordHash, Scope.Individual)
    }

    override suspend fun invalidateAccessToken() {
        prefs.get().remove(KEY_EXPIRE_TIME, Scope.Individual)
    }
}
