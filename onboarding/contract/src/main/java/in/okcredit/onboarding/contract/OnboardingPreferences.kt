package `in`.okcredit.onboarding.contract

interface OnboardingPreferences {

    companion object {
        const val KEY_NEW_USER = "KEY_NEW_USER"
        const val KEY_APP_LOCK_ENABLED = "KEY_SECURITY_ENABLED"
        const val KEY_INAPP_APP_LOCK_CANCELLED = "KEY_INAPP_APP_LOCK_CANCELLED"
    }

    fun isNewUser(): Boolean

    fun isAppLockEnabled(): Boolean

    suspend fun setInAppLockCancelled(value: Boolean)

    fun setUserSelectedLanguage(value: String)

    fun getUserSelectedLanguage(): String

    fun setIsFreshLogin(value: Boolean)

    suspend fun getPayablesOnboardingVariant(): String
    suspend fun setPayablesOnboardingVariant(value: String)

    suspend fun getVisibilityPreNetworkOnboardingNudge(): Boolean
    suspend fun setVisibilityPreNetworkOnboardingNudge(value: Boolean)
}
