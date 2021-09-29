package `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.views

import `in`.okcredit.frontend.R
import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.category_empty_placeholder_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class EmptyCategoryView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(ctx).inflate(R.layout.category_empty_placeholder_view, this, true)
    }

    interface Listener {
        fun clickedAddOtherCategory()
    }

    @ModelProp
    fun setTitle(searchQuery: String) {
        desc.text = Html.fromHtml(
            context.getString(
                R.string.category_empty_text,
                searchQuery
            )
        )
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        btn_add_category.clicks()
            .doOnNext {
                listener?.clickedAddOtherCategory()
            }
            .subscribe()
    }
}
