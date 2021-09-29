package `in`.okcredit.dynamicview.component.dashboard.cell_card

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import `in`.okcredit.fileupload._id.GlideApp
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyHolder
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

class CellCardComponentViewHolder : EpoxyHolder() {

    private lateinit var itemView: View
    private lateinit var title: TextView
    private lateinit var icon: ImageView
    private lateinit var clRoot: ConstraintLayout

    override fun bindView(itemView: View) {
        this.itemView = itemView
        title = itemView.findViewById(R.id.title)
        icon = itemView.findViewById(R.id.icon)
        clRoot = itemView.findViewById(R.id.cl_root)
    }

    fun render(component: CellCardComponentModel, clickListener: ComponentClickListener?) {
        setTextOrGone(component.title, title)
        if (component.icon != null && component.icon.isNotBlank()) loadImage(component.icon, icon) else icon.gone()
        component.bgColor?.let {
            try {
                clRoot.setBackgroundColor(Color.parseColor(it))
            } catch (_: IllegalArgumentException) {
            }
        }
        itemView.setOnClickListener { clickListener?.invoke() }

        component.metadata?.name?.let { name -> itemView.contentDescription = name }
    }

    private fun loadImage(url: String, imageView: ImageView) {
        imageView.visible()
        GlideApp.with(itemView.context)
            .load(url)
            .placeholder(ColorDrawable(ContextCompat.getColor(itemView.context, R.color.grey300)))
            .dontAnimate()
            .circleCrop()
            .into(imageView)
    }

    private fun setTextOrGone(text: String?, textView: TextView) {
        if (text.isNullOrEmpty().not()) {
            textView.text = text
            textView.visible()
        } else {
            textView.gone()
        }
    }
}
