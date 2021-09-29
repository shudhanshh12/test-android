package tech.okcredit.applock

import dagger.Lazy
import tech.okcredit.applock.analytics.AppLockEventTracker
import tech.okcredit.contract.AppLockTracker
import tech.okcredit.contract.Constants.SECURITY_PIN_CHANGED
import tech.okcredit.contract.FORGOT_PIN
import javax.inject.Inject

class AppLockTrackerImpl @Inject constructor(val appLockEventTracker: Lazy<AppLockEventTracker>) : AppLockTracker {
    override fun trackEvents(eventName: String, source: String?, screen: String?) {
        val entry = if (eventName == SECURITY_PIN_CHANGED) FORGOT_PIN else null
        appLockEventTracker.get().trackEvents(
            eventName = eventName,
            screen = screen,
            flow = source,
            entry = entry
        )
    }
}
