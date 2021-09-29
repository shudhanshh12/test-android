package tech.okcredit.android.auth

import io.reactivex.Observable

interface AuthLocalSource {

    fun observeAccessToken(): Observable<String>

    fun getMobile(): String?

    fun setMobile(mobile: String)

    fun getAccessToken(): String?

    fun getGrant(): Grant?

    fun setGrant(grant: Grant)

    fun getPasswordHash(): String?

    fun setPasswordHash(passwordHash: String)

    fun deleteAllExceptMobile()

    fun setCurrentMobileOTPToken(token: String)

    fun setNewMobileOtpToken(token: String)

    fun getNewMobileOtpToken(): String?

    fun getCurrentMobileOtpToken(): String?

    suspend fun invalidateAccessToken()
}
