package `in`.okcredit.dynamicview.view

import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyModel
import javax.inject.Inject

class DynamicViewController @Inject constructor() : AsyncEpoxyController() {

    private var component: EpoxyModel<Any>? = null

    fun render(component: EpoxyModel<Any>) {
        this.component = component
        requestModelBuild()
    }

    override fun buildModels() {
        component?.apply {
            id("parent_component")
            addTo(this@DynamicViewController)
        }
    }
}
