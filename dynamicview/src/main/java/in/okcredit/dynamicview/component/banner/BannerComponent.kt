package `in`.okcredit.dynamicview.component.banner

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class BannerComponent() :
    EpoxyModelWithHolder<BannerComponentViewHolder>() {

    @EpoxyAttribute
    var component: BannerComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_banner

    override fun createNewHolder() = BannerComponentViewHolder()

    override fun bind(holder: BannerComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
