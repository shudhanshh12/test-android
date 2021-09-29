package `in`.okcredit.dynamicview.events

import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigationDelegator
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ClickEventHandler @Inject constructor(
    private val tracker: DynamicViewEventTracker,
    private val internalDeeplinkNavigator: InternalDeeplinkNavigationDelegator
) {

    companion object {
        const val EVENT_KEY = "click"
    }

    fun onClick(component: ComponentModel) {
        val actions = component.eventHandlers?.get(EVENT_KEY) ?: return
        for (action in actions) {
            when (action) {
                is Action.Track -> tracker.track(action)
                is Action.Navigate -> navigate(action)
            }
        }
    }

    private fun navigate(action: Action.Navigate) {
        action.url?.let { internalDeeplinkNavigator.executeDeeplink(it) }
    }
}
