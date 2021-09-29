package `in`.okcredit.backend.contract

interface AppLockManager {

    fun isAppLockAuthReqd(): Boolean

    fun isAppLockActive(): Boolean

    fun authenticatePattern(pattern: String): Boolean

    fun enableAppLock(pattern: String?)

    fun disableAppLock(oldPatternAttempt: String): Boolean

    fun clearAppLockData()

    fun setLocked(isLocked: Boolean)
}
