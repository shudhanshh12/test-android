package `in`.okcredit.shared.deeplink

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class InternalDeeplinkNavigationDelegator @Inject constructor(
    private val activity: AppCompatActivity,
    private val internalDeeplinkNavigator: InternalDeeplinkNavigator
) {
    fun executeDeeplink(deeplink: String) = internalDeeplinkNavigator.executeDeeplink(deeplink, activity)
}
