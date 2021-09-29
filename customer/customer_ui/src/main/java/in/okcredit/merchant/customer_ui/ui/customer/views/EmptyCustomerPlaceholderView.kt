package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.merchant.customer_ui.databinding.CustomerScreenEmptyViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class EmptyCustomerPlaceholderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: CustomerScreenEmptyViewBinding =
        CustomerScreenEmptyViewBinding.inflate(LayoutInflater.from(context), this, true)
}
