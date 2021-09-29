package `in`.okcredit.dynamicview.component.dashboard.banner_card

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class BannerCardComponent : EpoxyModelWithHolder<BannerCardComponentViewHolder>() {

    @EpoxyAttribute
    var component: BannerCardComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_banner_card

    override fun createNewHolder() = BannerCardComponentViewHolder()

    override fun bind(holder: BannerCardComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
