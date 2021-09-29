package `in`.okcredit.dynamicview.view

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.DynamicViewKit
import `in`.okcredit.dynamicview.TargetSpec
import `in`.okcredit.dynamicview.data.model.ComponentModel
import android.view.View
import com.airbnb.epoxy.EpoxyHolder

class DynamicViewHolder(private val dynamicViewKit: DynamicViewKit) : EpoxyHolder() {

    private lateinit var itemView: DynamicView

    override fun bindView(itemView: View) {
        this.itemView = itemView as DynamicView
    }

    fun render(
        componentModel: ComponentModel?,
        spec: TargetSpec,
        clickListener: ComponentClickListener? = null
    ) {
        dynamicViewKit.render(itemView, componentModel, spec, clickListener)
    }
}
