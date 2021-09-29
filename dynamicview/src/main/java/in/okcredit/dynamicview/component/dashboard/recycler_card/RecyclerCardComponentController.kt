package `in`.okcredit.dynamicview.component.dashboard.recycler_card

import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyModel

class RecyclerCardComponentController : AsyncEpoxyController() {

    private var components = listOf<EpoxyModel<Any>>()

    fun render(components: List<EpoxyModel<Any>>) {
        this.components = components
        requestModelBuild()
    }

    override fun buildModels() {
        components.forEach {
            it.apply {
                id("child_component_$modelCountBuiltSoFar")
                addTo(this@RecyclerCardComponentController)
            }
        }
    }
}
