package `in`.okcredit.dynamicview.component.recycler

import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyModel

class RecyclerComponentController : AsyncEpoxyController() {

    private var components = listOf<EpoxyModel<Any>>()

    fun render(components: List<EpoxyModel<Any>>) {
        this.components = components
        requestModelBuild()
    }

    override fun buildModels() {
        components.forEach {
            it.apply {
                id("child_component_$modelCountBuiltSoFar")
                addTo(this@RecyclerComponentController)
            }
        }
    }
}
