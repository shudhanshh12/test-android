package `in`.okcredit.dynamicview.component.cell

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import `in`.okcredit.fileupload._id.GlideApp
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyHolder

class CellComponentViewHolder() : EpoxyHolder() {

    private lateinit var itemView: View
    lateinit var title: TextView
    lateinit var icon: ImageView

    override fun bindView(itemView: View) {
        this.itemView = itemView
        title = itemView.findViewById(R.id.title)
        icon = itemView.findViewById(R.id.icon)
    }

    fun render(component: CellComponentModel, clickListener: ComponentClickListener?) {
        title.text = component.title

        GlideApp.with(icon)
            .load(component.icon)
            .placeholder(ColorDrawable(ContextCompat.getColor(itemView.context, R.color.grey300)))
            .thumbnail(0.25f)
            .into(icon)

        itemView.setOnClickListener { clickListener?.invoke() }

        component.metadata?.name?.let { name -> itemView.contentDescription = name }
    }
}
