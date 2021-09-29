package `in`.okcredit.dynamicview.component.dashboard.advertisement

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
import com.google.android.material.button.MaterialButton
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

class AdvertisementComponentViewHolder : EpoxyHolder() {

    private lateinit var itemView: View
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var icon: ImageView
    private lateinit var button: MaterialButton
    private lateinit var image: ImageView
    private lateinit var clRoot: ConstraintLayout

    override fun bindView(itemView: View) {
        this.itemView = itemView
        title = itemView.findViewById(R.id.title)
        subtitle = itemView.findViewById(R.id.subtitle)
        icon = itemView.findViewById(R.id.icon)
        button = itemView.findViewById(R.id.cta)
        image = itemView.findViewById(R.id.image)
        clRoot = itemView.findViewById(R.id.cl_root)
    }

    fun render(component: AdvertisementComponentModel, clickListener: ComponentClickListener? = null) {
        if (component.icon.isNullOrEmpty().not()) loadImage(component.icon, icon) else icon.gone()

        title.text = component.title

        if (component.subtitle.isNullOrEmpty().not()) {
            subtitle.visible()
            subtitle.text = component.subtitle
        } else {
            subtitle.gone()
        }

        if (component.image.isNullOrEmpty().not()) loadImage(component.image, image) else image.gone()

        if (component.buttonText.isNullOrEmpty().not()) {
            button.visible()
            button.text = component.buttonText
            button.setOnClickListener { clickListener?.invoke() }
            itemView.setOnClickListener { clickListener?.invoke() }
            itemView.isClickable = true
        } else {
            button.gone()
            button.setOnClickListener(null)
            itemView.setOnClickListener(null)
            itemView.isClickable = false
        }

        component.bgColor?.let {
            try {
                clRoot.setBackgroundColor(Color.parseColor(it))
            } catch (_: IllegalArgumentException) {
            }
        } ?: clRoot.setBackgroundColor(Color.WHITE)

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
