package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.merchant.customer_ui.databinding.CustomerFragmentDateViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class DateView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val binding: CustomerFragmentDateViewBinding =
        CustomerFragmentDateViewBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setDate(dateString: String) {
        binding.date.text = dateString
    }
}
