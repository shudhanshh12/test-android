package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.CustomerFragmentPrivacyViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PrivacyView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun onPrivacyClicked()
    }

    private val binding: CustomerFragmentPrivacyViewBinding =
        CustomerFragmentPrivacyViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.content.text = context?.getString(R.string.all_transactions_with_your)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        binding.content.clicks()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onPrivacyClicked() }
            .subscribe()
    }
}
