package `in`.okcredit.dynamicview.component.dashboard.recycler_card

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class RecyclerCardComponent(
    private val environment: Environment,
    private val component: RecyclerCardComponentModel
) :
    EpoxyModelWithHolder<RecyclerCardComponentViewHolder>() {

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_recycler_card

    override fun createNewHolder() = RecyclerCardComponentViewHolder()

    override fun bind(holder: RecyclerCardComponentViewHolder) {
        super.bind(holder)
        holder.render(environment, component, clickListener)
    }
}
