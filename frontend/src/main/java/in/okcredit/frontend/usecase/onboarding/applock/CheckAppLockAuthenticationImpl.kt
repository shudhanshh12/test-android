package `in`.okcredit.frontend.usecase.onboarding.applock

import `in`.okcredit.backend.contract.AppLockManager
import `in`.okcredit.backend.contract.CheckAuth
import `in`.okcredit.frontend.contract.CheckAppLockAuthentication
import `in`.okcredit.frontend.contract.data.AppResume
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.AppVariable
import javax.inject.Inject

/**
 *  This class is used to check resume session of app like whether user is
 *  1. authenticated or not [NOT_AUTHENTICAED]
 *  2. business name  is filled by user (new registration user) [BUSINESS_NAME]
 *  3. app lock is setup by login-user(old user) when they have applock feature ON [APP_LOCK_SETUP]
 *  4. authenticate user via app lock when app was in background for 20 minutes OR app was killed [NEW_APP_LOCK_RESUME] [OLD_APP_LOCK_RESUME]
 *  5. [NONE] when no screen has to be opened
 *
 *  This helps to display appropriate screen when app is opened from anywhere (user opened by clicking on launcher icon Or via deeplink)
 */

class CheckAppLockAuthenticationImpl @Inject constructor(
    private val appVariable: Lazy<AppVariable>,
    private var appLockManager: Lazy<AppLockManager>,
    private val onboardingPreferences: Lazy<OnboardingPreferencesImpl>,
    private val checkAuth: Lazy<CheckAuth>
) : CheckAppLockAuthentication {

    override fun execute(): Single<AppResume> {
        return isUserAuthenticated()
            .map {
                if (it) {
                    return@map checkUserFlow()
                } else {
                    return@map AppResume.NOT_AUTHENTICAED
                }
            }
    }

    private fun checkUserFlow(): AppResume {
        val newUser = onboardingPreferences.get().isNewUser()
        val businessNameSkipped = onboardingPreferences.get().hasSkippedNameScreen()
        val hasAppLockFeature = onboardingPreferences.get().existingUserEnabledAppLock()
        val newAppLockEnabled = onboardingPreferences.get().isAppLockEnabled()
        val loginUserAppLockNotEnabled = hasAppLockFeature && !newAppLockEnabled
        return if (newUser && businessNameSkipped) {
            checkAppResumeLock()
        } else if (newUser && !businessNameSkipped && !onboardingPreferences.get().hasNameEntered()) {
            AppResume.BUSINESS_NAME
        } else if (loginUserAppLockNotEnabled) {
            AppResume.APP_LOCK_SETUP
        } else {
            checkAppResumeLock()
        }
    }

    private fun checkAppResumeLock(): AppResume {
        val newAppLockEnabled = onboardingPreferences.get().isAppLockEnabled()
        val oldAppLockEnabled: Boolean = appLockManager.get().isAppLockActive()
        val resumeAppSession: Boolean = onboardingPreferences.get().wasAppInBackgroundFor20Minutes()
        return if (oldAppLockEnabled && (resumeAppSession || appLockManager.get().isAppLockAuthReqd())) {
            AppResume.OLD_APP_LOCK_RESUME
        } else if ((newAppLockEnabled && appVariable.get().appCreated) || (newAppLockEnabled && resumeAppSession)) {
            AppResume.NEW_APP_LOCK_RESUME
        } else {
            AppResume.NONE
        }
    }

    private fun isUserAuthenticated(): Single<Boolean> {
        return checkAuth.get().execute().firstOrError()
    }
}
