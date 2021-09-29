package `in`.okcredit.dynamicview.component.dashboard.recycler_card

import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.events.ClickEventHandler
import `in`.okcredit.dynamicview.events.ViewEventHandler
import android.content.Context
import com.airbnb.epoxy.EpoxyModel
import dagger.Reusable
import javax.inject.Inject

@Reusable
class RecyclerCardComponentFactory @Inject constructor(
    private val viewEventHandler: ViewEventHandler,
    private val clickEventHandler: ClickEventHandler
) : ComponentFactory {

    override fun create(
        context: Context,
        environment: Environment,
        component: ComponentModel
    ) = RecyclerCardComponent_(environment, component as RecyclerCardComponentModel).apply {
        onVisibilityStateChanged { _, _, visibilityState ->
            viewEventHandler.onVisibilityStateChanged(visibilityState, environment, component)
        }
        clickListener = {
            clickEventHandler.onClick(component)
            environment.clickListener?.invoke()
        }
    } as EpoxyModel<Any>
}
