package `in`.okcredit.dynamicview.component.toolbar

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import `in`.okcredit.fileupload._id.GlideApp
import android.view.View
import android.widget.ImageView
import com.airbnb.epoxy.EpoxyHolder
import tech.okcredit.android.base.extensions.getColorDrawable

class ToolbarComponentViewHolder : EpoxyHolder() {

    private lateinit var itemView: View
    lateinit var icon: ImageView

    override fun bindView(itemView: View) {
        this.itemView = itemView
        icon = itemView.findViewById(R.id.icon)
    }

    fun render(component: ToolbarComponentModel, clickListener: ComponentClickListener? = null) {
        val drawable = itemView.getColorDrawable(R.color.grey300)
        GlideApp.with(itemView)
            .load(component.icon)
            .placeholder(drawable)
            .error(drawable)
            .fallback(drawable)
            .thumbnail(0.2f)
            .into(icon)

        itemView.setOnClickListener { clickListener?.invoke() }

        component.metadata?.name?.let { name -> itemView.contentDescription = name }
    }
}
