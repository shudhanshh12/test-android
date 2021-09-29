package `in`.okcredit.dynamicview.component.dashboard.cell_card

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class CellCardComponent : EpoxyModelWithHolder<CellCardComponentViewHolder>() {

    @EpoxyAttribute
    var component: CellCardComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_cell_card

    override fun createNewHolder() = CellCardComponentViewHolder()

    override fun bind(holder: CellCardComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
