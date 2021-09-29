package `in`.okcredit.dynamicview.component.dashboard.cell2

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class Cell2Component : EpoxyModelWithHolder<Cell2ComponentViewHolder>() {

    @EpoxyAttribute
    var component: Cell2ComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_cell2

    override fun createNewHolder() = Cell2ComponentViewHolder()

    override fun bind(holder: Cell2ComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
