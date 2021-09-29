package `in`.okcredit.dynamicview

import `in`.okcredit.dynamicview.data.model.ComponentModel

data class TargetSpec(
    val name: String,
    val allowedComponents: Set<Class<out ComponentModel>> = hashSetOf(),
    val allowAllComponents: Boolean = false, // allowAllComponents will override allowedComponents
    val trackViewEvents: Boolean = true // track component view events in ViewEventHandler
)
