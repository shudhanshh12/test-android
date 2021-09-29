package `in`.okcredit.dynamicview.component.dashboard.cell2

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import `in`.okcredit.fileupload._id.GlideApp
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyHolder
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

class Cell2ComponentViewHolder : EpoxyHolder() {

    private lateinit var itemView: View
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var icon: ImageView

    override fun bindView(itemView: View) {
        this.itemView = itemView
        title = itemView.findViewById(R.id.title)
        subtitle = itemView.findViewById(R.id.subtitle)
        icon = itemView.findViewById(R.id.icon)
    }

    fun render(component: Cell2ComponentModel, clickListener: ComponentClickListener?) {
        setTextOrGone(component.title, title)
        setTextOrGone(component.subtitle, subtitle)

        loadImage(component, icon)
        itemView.setOnClickListener { clickListener?.invoke() }

        component.metadata?.name?.let { name -> itemView.contentDescription = name }
    }

    private fun loadImage(component: Cell2ComponentModel, imageView: ImageView) {
        val defaultPic = component.title?.let {
            TextDrawable.builder().buildRound(it.substring(0, 1), ColorGenerator.MATERIAL.getColor(it))
        }
        if (component.icon != null) {
            val req = GlideApp.with(itemView.context)
                .load(component.icon)
                .circleCrop()
            defaultPic?.let { req.placeholder(defaultPic).fallback(defaultPic) }
            req.into(imageView)
        } else {
            imageView.setImageDrawable(defaultPic)
        }
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
