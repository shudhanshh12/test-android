package `in`.okcredit.dynamicview.component.dashboard.advertisement

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class AdvertisementComponent : EpoxyModelWithHolder<AdvertisementComponentViewHolder>() {

    @EpoxyAttribute
    var component: AdvertisementComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_advertisement

    override fun createNewHolder() = AdvertisementComponentViewHolder()

    override fun bind(holder: AdvertisementComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
