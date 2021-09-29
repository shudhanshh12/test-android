package `in`.okcredit.merchant.customer_ui.ui.add_discount.views

import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import kotlinx.android.synthetic.main.item_add_bill_margin_8.view.*

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class AddBillsView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    interface Listener {
        fun onAddBillsClicked()
    }

    init {
        LayoutInflater.from(ctx).inflate(R.layout.item_add_bill_margin_8, this, true)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        add_bill_textview.clicks()
            .doOnNext {
                listener?.onAddBillsClicked()
            }
            .subscribe()
    }
}
