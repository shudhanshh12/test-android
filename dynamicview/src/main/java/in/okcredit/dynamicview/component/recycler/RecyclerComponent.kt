package `in`.okcredit.dynamicview.component.recycler

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.Environment
import `in`.okcredit.dynamicview.R
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class RecyclerComponent(private val environment: Environment, private val component: RecyclerComponentModel) :
    EpoxyModelWithHolder<RecyclerComponentViewHolder>() {

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.component_recycler

    override fun createNewHolder() = RecyclerComponentViewHolder()

    override fun bind(holder: RecyclerComponentViewHolder) {
        super.bind(holder)
        holder.render(environment, component, clickListener)
    }
}
