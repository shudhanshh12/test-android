package `in`.okcredit.frontend.ui.add_supplier_transaction.views

import `in`.okcredit.merchant.customer_ui.databinding.AddTxnFragmentPermissionViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PermissionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun onAllowPermissionClicked()
    }

    private val binding: AddTxnFragmentPermissionViewBinding =
        AddTxnFragmentPermissionViewBinding.inflate(LayoutInflater.from(context), this)

    @CallbackProp
    fun setListener(listener: Listener?) {
        binding.btnAllow.setOnClickListener { listener?.onAllowPermissionClicked() }
    }
}
