package `in`.okcredit.dynamicview

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel

data class Environment(
    val targetSpec: TargetSpec,
    val componentFactory: ComponentFactory,
    val clickListener: ComponentClickListener? = null
)

fun Environment.isValidComponent(component: ComponentModel?) =
    component != null && (targetSpec.allowedComponents.contains(component.javaClass) || targetSpec.allowAllComponents)
