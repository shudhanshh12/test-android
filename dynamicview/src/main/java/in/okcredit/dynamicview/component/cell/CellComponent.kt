package `in`.okcredit.dynamicview.component.cell

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class CellComponent() : EpoxyModelWithHolder<CellComponentViewHolder>() {

    @EpoxyAttribute
    var component: CellComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_cell

    override fun createNewHolder() = CellComponentViewHolder()

    override fun bind(holder: CellComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
