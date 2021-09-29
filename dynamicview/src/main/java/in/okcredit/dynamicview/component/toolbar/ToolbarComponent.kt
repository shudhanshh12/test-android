package `in`.okcredit.dynamicview.component.toolbar

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class ToolbarComponent() :
    EpoxyModelWithHolder<ToolbarComponentViewHolder>() {

    @EpoxyAttribute
    var component: ToolbarComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_toolbar

    override fun createNewHolder() = ToolbarComponentViewHolder()

    override fun bind(holder: ToolbarComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
