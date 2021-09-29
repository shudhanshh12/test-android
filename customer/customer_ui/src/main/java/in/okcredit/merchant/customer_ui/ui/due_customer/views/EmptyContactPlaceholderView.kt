package `in`.okcredit.merchant.customer_ui.ui.due_customer.views

import `in`.okcredit.merchant.customer_ui.databinding.AddCustomerFragmentEmptyViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class EmptyContactPlaceholderView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val binding: AddCustomerFragmentEmptyViewBinding =
        AddCustomerFragmentEmptyViewBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setSearchName(message: String?) {
        binding.emptyTxText.text = message
    }
}
