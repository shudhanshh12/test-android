package `in`.okcredit.communication_inappnotification.eventhandler

import `in`.okcredit.communication_inappnotification.analytics.InAppNotificationTracker
import `in`.okcredit.communication_inappnotification.model.Action
import `in`.okcredit.communication_inappnotification.model.ActionButton
import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Lazy
import tech.okcredit.android.base.extensions.ifLet
import java.lang.ref.WeakReference
import javax.inject.Inject

class ClickEventHandler @Inject constructor(
    private val tracker: Lazy<InAppNotificationTracker>,
    private val internalDeeplinkNavigator: Lazy<InternalDeeplinkNavigator>
) {

    companion object {
        const val EVENT_KEY = "click"
    }

    fun onClick(actionButton: ActionButton, bottomSheetWeak: WeakReference<BottomSheetDialogFragment>) {
        val actions = actionButton.clickHandlers ?: return
        for (action in actions) {
            when (action) {
                is Action.Track -> track(action)
                is Action.Navigate -> navigate(action, bottomSheetWeak)
                is Action.Dismiss -> dismiss(bottomSheetWeak)
            }
        }
    }

    private fun track(action: Action.Track) = tracker.get().track(action.event, action.properties)

    private fun dismiss(bottomSheetWeak: WeakReference<BottomSheetDialogFragment>) = bottomSheetWeak.get()?.dismiss()

    private fun navigate(action: Action.Navigate, bottomSheetWeak: WeakReference<BottomSheetDialogFragment>) {
        val fragmentActivity = bottomSheetWeak.get()?.requireActivity()
        ifLet(action.url, fragmentActivity) { url, activity ->
            internalDeeplinkNavigator.get().executeDeeplink(url, activity)
        }
    }
}
