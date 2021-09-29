package `in`.okcredit.frontend.ui.live_sales.views

import `in`.okcredit.frontend.R
import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.Nullable
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.livesales_fragment_empty_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class EmptyPlaceholderView : FrameLayout {

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
    }

    @ModelProp
    fun setCustomerName(name: String?) {
        empty_tx_text.text =
            Html.fromHtml(context.getString(R.string.your_transactions_with_customer_name_is_safe_and_secure, name))
    }

    private fun initView() {
        View.inflate(context, R.layout.livesales_fragment_empty_view, this)
    }
}
