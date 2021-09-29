package `in`.okcredit.dynamicview.component

import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.data.model.ComponentModel
import android.content.Context
import com.airbnb.epoxy.EpoxyModel

interface ComponentFactory {
    fun create(
        context: Context,
        environment: Environment,
        component: ComponentModel
    ): EpoxyModel<Any>?
}
