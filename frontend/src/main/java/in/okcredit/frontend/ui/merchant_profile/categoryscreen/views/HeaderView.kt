package `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.views

import `in`.okcredit.frontend.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.category_header_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class HeaderView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(ctx).inflate(R.layout.category_header_view, this, true)
    }

    @ModelProp
    fun setTitle(text: String) {
        title.text = text
    }
}
