package `in`.okcredit.merchant.customer_ui.ui.add_discount.views

import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.add_txn_fragment_permission_view.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class PermissionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun onAllowPermissionClicked()
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.add_txn_fragment_permission_view, this, true)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        btn_allow.setOnClickListener { listener?.onAllowPermissionClicked() }
    }
}
