package `in`.okcredit.dynamicview.component.dashboard.summary_card

import `in`.okcredit.dynamicview.ComponentClickListener
import `in`.okcredit.dynamicview.R
import `in`.okcredit.fileupload._id.GlideApp
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyHolder
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

class SummaryCardComponentViewHolder : EpoxyHolder() {

    private lateinit var itemView: View
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var icon: ImageView
    private lateinit var value: TextView
    private lateinit var valueDescription: TextView
    private lateinit var icon2: ImageView

    override fun bindView(itemView: View) {
        this.itemView = itemView
        title = itemView.findViewById(R.id.title)
        subtitle = itemView.findViewById(R.id.subtitle)
        icon = itemView.findViewById(R.id.icon)
        value = itemView.findViewById(R.id.value)
        valueDescription = itemView.findViewById(R.id.value_description)
        icon2 = itemView.findViewById(R.id.icon_right_chevron)
    }

    fun render(component: SummaryCardComponentModel, clickListener: ComponentClickListener? = null) {
        if (component.icon.isNullOrEmpty().not()) loadImage(component.icon, icon) else icon.gone()
        title.text = component.title
        if (component.subtitle.isNullOrEmpty().not()) {
            subtitle.text = component.subtitle
            subtitle.visible()
        } else {
            subtitle.gone()
        }
        component.value?.let { TempCurrencyUtil.renderV2(it, value, true) }
        component.valueDescription?.let {
            valueDescription.text = it
            valueDescription.visible()
        } ?: valueDescription.gone()
        itemView.setOnClickListener { clickListener?.invoke() }

        component.metadata?.name?.let { name -> itemView.contentDescription = name }
    }

    private fun loadImage(url: String?, imageView: ImageView) {
        url?.let {
            imageView.visible()
            GlideApp.with(itemView.context)
                .load(url)
                .placeholder(ColorDrawable(ContextCompat.getColor(itemView.context, R.color.grey300)))
                .dontAnimate()
                .into(imageView)
        }
    }
}
