package `in`.okcredit.dynamicview.component.menu

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class MenuComponent() : EpoxyModelWithHolder<MenuComponentViewHolder>() {

    @EpoxyAttribute
    var component: MenuComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_menu

    override fun createNewHolder() = MenuComponentViewHolder()

    override fun bind(holder: MenuComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
