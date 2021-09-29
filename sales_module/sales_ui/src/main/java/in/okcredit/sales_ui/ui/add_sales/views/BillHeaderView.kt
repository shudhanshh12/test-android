package `in`.okcredit.sales_ui.ui.add_sales.views

import `in`.okcredit.sales_ui.databinding.ItemBillHeaderBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BillHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val inflater = LayoutInflater.from(context)
        ItemBillHeaderBinding.inflate(inflater, this, true)
    }
}
