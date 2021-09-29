package `in`.okcredit.dynamicview

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import `in`.okcredit.dynamicview.view.DynamicView
import javax.inject.Inject

class DynamicViewKit @Inject constructor(
    private val componentFactory: ComponentFactory
) {

    fun render(
        view: DynamicView,
        component: ComponentModel?,
        spec: TargetSpec,
        clickListener: ComponentClickListener? = null
    ) {
        val environment = Environment(spec, componentFactory, clickListener)
        view.render(environment, component)
    }
}
