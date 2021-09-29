package tech.okcredit.help.help_main.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.help_section.view.*
import tech.okcredit.help.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HelpSectionView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.help_section, this, true)
    }

    lateinit var helpItemId: String

    @ModelProp
    fun setHelpSectionItemText(helpSectionItemTitle: String?) {
        tv_help_item_heading.text = helpSectionItemTitle
    }

    @ModelProp
    fun setItemId(helpItemId: String) {
        this.helpItemId = helpItemId
    }

    @ModelProp
    fun isFinalItem(visibility: Boolean) {
        divider.visibility = if (visibility) View.GONE else View.VISIBLE
    }

    interface OnHelpSectionItemTitleClick {
        fun onItemClick(helpItemId: String)
    }

    @CallbackProp
    fun sectionClick(listener: OnHelpSectionItemTitleClick?) {
        sectionRoot.setOnClickListener {
            listener?.onItemClick(helpItemId)
        }
    }
}
