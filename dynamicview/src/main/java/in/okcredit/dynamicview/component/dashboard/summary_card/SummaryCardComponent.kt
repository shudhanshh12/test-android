package `in`.okcredit.dynamicview.component.dashboard.summary_card

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class SummaryCardComponent : EpoxyModelWithHolder<SummaryCardComponentViewHolder>() {

    @EpoxyAttribute
    var component: SummaryCardComponentModel? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_summary_card

    override fun createNewHolder() = SummaryCardComponentViewHolder()

    override fun bind(holder: SummaryCardComponentViewHolder) {
        super.bind(holder)
        component?.let {
            holder.render(it, clickListener)
        }
    }
}
