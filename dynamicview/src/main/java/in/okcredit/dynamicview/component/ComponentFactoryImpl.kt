package `in`.okcredit.dynamicview.component

import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.data.model.ComponentModel
import android.content.Context
import javax.inject.Inject

class ComponentFactoryImpl @Inject constructor(
    private val factories: Map<String, @JvmSuppressWildcards ComponentFactory>
) : ComponentFactory {

    override fun create(context: Context, environment: Environment, component: ComponentModel) =
        factories[component.kind]?.create(context, environment, component)
}
