package tech.okcredit.home.ui.settings.usecase

import `in`.okcredit.backend.contract.AppLockManager
import `in`.okcredit.onboarding.contract.OnboardingPreferences
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.home.utils.CUSTOM_APP_LOCK
import tech.okcredit.home.utils.NO_APP_LOCK
import tech.okcredit.home.utils.SYSTEM_APP_LOCK
import javax.inject.Inject

class CheckAppLock @Inject constructor(
    private val onboardingPreferences: Lazy<OnboardingPreferences>,
    private val appLockManager: Lazy<AppLockManager>
) {
    fun execute(): Observable<Pair<Boolean, String>> {
        if (appLockManager.get().isAppLockActive()) {
            return Observable.just(Pair(true, CUSTOM_APP_LOCK))
        } else {
            if (onboardingPreferences.get().isAppLockEnabled()) {
                return Observable.just(Pair(true, SYSTEM_APP_LOCK))
            } else {
                return Observable.just(Pair(false, NO_APP_LOCK))
            }
        }
    }
}
