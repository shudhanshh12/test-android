package `in`.okcredit.sales_ui.ui.add_sales.views

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.databinding.ItemBillBinding
import `in`.okcredit.sales_ui.utils.SalesUtil
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BillView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ItemBillBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ItemBillBinding.inflate(inflater, this, true)
    }

    @ModelProp
    fun setBillItem(billItem: BillModel.BillItem) {
        binding.itemName.text = billItem.name
        binding.rate.text = context.getString(R.string.rupees, SalesUtil.currencyDisplayFormat(billItem.rate))
        val qty = SalesUtil.displayDecimalNumber(billItem.quantity)
        binding.qty.text = qty
        binding.total.text = context.getString(
            R.string.rupees,
            SalesUtil.currencyDisplayFormat(billItem.rate * billItem.quantity)
        )
    }
}
