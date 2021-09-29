package `in`.okcredit.dynamicview.view

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.DynamicViewKit
import `in`.okcredit.dynamicview.R
import `in`.okcredit.dynamicview.TargetSpec
import `in`.okcredit.dynamicview.data.model.ComponentModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder

@EpoxyModelClass
open class DynamicViewEpoxyItem @JvmOverloads constructor(
    private val dynamicViewKit: DynamicViewKit
) : EpoxyModelWithHolder<DynamicViewHolder>() {

    @EpoxyAttribute
    var component: ComponentModel? = null

    @EpoxyAttribute
    var spec: TargetSpec? = null

    @EpoxyAttribute
    var clickListener: ComponentClickListener? = null

    override fun getDefaultLayout() = R.layout.item_dynamic_view

    override fun createNewHolder() = DynamicViewHolder(dynamicViewKit)

    override fun bind(holder: DynamicViewHolder) {
        super.bind(holder)
        spec?.let {
            holder.render(component, spec!!, clickListener)
        }
    }
}
