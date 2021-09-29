package `in`.okcredit.dynamicview.component.dashboard.cell2

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
class Cell2ComponentFactory @Inject constructor(
    private val viewEventHandler: ViewEventHandler,
    private val clickEventHandler: ClickEventHandler
) : ComponentFactory {

    override fun create(
        context: Context,
        environment: Environment,
        component: ComponentModel
    ) = Cell2Component_().apply {
        component(component as Cell2ComponentModel)
        clickListener = {
            clickEventHandler.onClick(component)
            environment.clickListener?.invoke()
        }
        onVisibilityStateChanged { _, _, visibilityState ->
            viewEventHandler.onVisibilityStateChanged(visibilityState, environment, component)
        }
    } as EpoxyModel<Any>
}
