package `in`.okcredit.dynamicview.events

import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.data.model.Action
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.data.model.withDefaultProperties
import com.airbnb.epoxy.VisibilityState
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ViewEventHandler @Inject constructor(private val tracker: DynamicViewEventTracker) {

    companion object {
        const val EVENT_KEY = "view"
    }

    fun onVisibilityStateChanged(visibilityState: Int, environment: Environment, component: ComponentModel) {
        if (environment.targetSpec.trackViewEvents && visibilityState == VisibilityState.FOCUSED_VISIBLE) {
            trackViewEvent(environment.targetSpec.name, component)
        }
    }

    fun trackViewEvent(targetName: String, component: ComponentModel) {
        val actions = component.eventHandlers?.get(EVENT_KEY) ?: return
        for (action in actions) {
            when (action) {
                is Action.Track -> tracker.track(
                    action.withDefaultProperties(targetName, component)
                )
            }
        }
    }
}
