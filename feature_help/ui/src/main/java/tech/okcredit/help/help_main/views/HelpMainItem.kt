package tech.okcredit.help.help_main.views

import `in`.okcredit.fileupload._id.GlideApp
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.help_main_item.view.*
import tech.okcredit.help.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HelpMainItem @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.help_main_item, this, true)
    }

    private lateinit var sectionId: String
    @ModelProp
    fun setHelpSectionIcon(imageUrl: String?) {
        val placeHolder = ContextCompat.getDrawable(context, R.drawable.circle_shape_grey_filled)
        GlideApp.with(context)
            .load(imageUrl)
            .placeholder(placeHolder)
            .into(iv_help_text_icon)
    }

    @ModelProp
    fun setHelpText(helpText: String?) {
        tv_help_text.text = helpText
    }

    @ModelProp
    fun setIsExpanded(isExpanded: Boolean) {
        if (isExpanded) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.green_lite_1))
            tv_help_text.setTextColor(ContextCompat.getColor(context, R.color.grey900))
            iv_arrow_right.setImageResource(R.drawable.ic_down_arrow_white)
            iv_arrow_right.setColorFilter(ContextCompat.getColor(context, R.color.primary_dark))
        } else {
            rootLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            tv_help_text.setTextColor(ContextCompat.getColor(context, R.color.grey900))
            iv_arrow_right.setImageResource(R.drawable.ic_arrow_right)
        }
    }

    interface IHelpExpandClick {
        fun OnExpandClick(item: String)
    }

    @ModelProp
    fun setHelpSectionId(sectionId: String) {
        this.sectionId = sectionId
    }

    @CallbackProp
    fun onExpandIconClick(listener: IHelpExpandClick?) {
        rootLayout.setOnClickListener {
            listener?.OnExpandClick(sectionId)
        }
    }
}
