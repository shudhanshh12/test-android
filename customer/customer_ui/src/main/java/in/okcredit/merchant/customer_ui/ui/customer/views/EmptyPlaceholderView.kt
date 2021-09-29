package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.EmptyPlaceHolderViewBinding
import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class EmptyPlaceholderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: EmptyPlaceHolderViewBinding =
        EmptyPlaceHolderViewBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setCustomerName(name: String?) {
        binding.emptyTxText.text =
            Html.fromHtml(context.getString(R.string.your_transactions_with_customer_name_is_safe_and_secure, name))
    }
}
