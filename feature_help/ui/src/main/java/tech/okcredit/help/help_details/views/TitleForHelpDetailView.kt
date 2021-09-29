package tech.okcredit.help.help_details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.title_for_help_detail_view.view.*
import tech.okcredit.help.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TitleForHelpDetailView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.title_for_help_detail_view, this, true)
    }

    @ModelProp
    fun setTitleForHelpDetail(value: String) {
        tv_help_detail_item_title.text = value
    }
}
