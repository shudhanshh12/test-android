package `in`.okcredit.sales_ui.ui.add_bill_items.views

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.databinding.ItemAddBillItemBinding
import `in`.okcredit.sales_ui.utils.SalesUtil
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AddBillView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: ItemAddBillItemBinding

    data class Model(val billItem: BillModel.BillItem, val isSearchItem: Boolean = false)

    private var listener: Listener? = null

    interface Listener {
        fun onAdd(billItem: BillModel.BillItem)
        fun onAddQuantity(billItem: BillModel.BillItem)
        fun onSubtractQuantity(billItem: BillModel.BillItem)
        fun onDelete(billItem: BillModel.BillItem)
        fun onTap(billItem: BillModel.BillItem, isSearchItem: Boolean)
    }

    init {
        val inflater = LayoutInflater.from(context)
        binding = ItemAddBillItemBinding.inflate(inflater, this, true)
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    @ModelProp
    fun setBillItem(model: Model) {
        binding.name.text = model.billItem.name
        binding.rate.text = if (model.isSearchItem)
            context.getString(R.string.tap_to_add_rate)
        else context.getString(R.string.set_rate, SalesUtil.currencyDisplayFormat(model.billItem.rate))
        if (model.billItem.quantity > 0) {
            binding.quantityLayout.background = resources.getDrawable(R.drawable.circle_background_green_primary_outline)
            binding.minus.visibility = View.VISIBLE
            binding.quantityEditText.visibility = View.VISIBLE
            binding.add.visibility = View.GONE
            binding.quantityEditText.setText(SalesUtil.displayDecimalNumber(model.billItem.quantity))
            if (model.billItem.quantity > 1) {
                binding.minus.setImageResource(R.drawable.ic_minus)
                binding.minus.imageTintList = ContextCompat.getColorStateList(context, R.color.green_primary)
            } else {
                binding.minus.setImageResource(R.drawable.ic_delete_bill)
                binding.minus.imageTintList = ContextCompat.getColorStateList(context, R.color.grey400)
            }
        } else {
            binding.quantityLayout.background = resources.getDrawable(R.drawable.circle_background_grey100_outline)
            binding.minus.visibility = View.GONE
            binding.quantityEditText.visibility = View.GONE
            binding.add.visibility = View.VISIBLE
        }
        binding.plus.setOnClickListener {
            if (model.billItem.quantity > 0) {
                listener?.onAddQuantity(model.billItem)
            } else if (model.isSearchItem.not()) {
                listener?.onAdd(model.billItem)
            } else {
                listener?.onTap(model.billItem, model.isSearchItem)
            }
        }
        binding.minus.setOnClickListener {
            if (model.billItem.quantity > 1) {
                listener?.onSubtractQuantity(model.billItem)
            } else {
                listener?.onDelete(model.billItem)
            }
        }
        binding.quantityLayout.setOnClickListener {
            if (model.isSearchItem.not() && model.billItem.quantity == 0.0) {
                listener?.onAdd(model.billItem)
            } else if (model.isSearchItem) {
                listener?.onTap(model.billItem, model.isSearchItem)
            }
        }
        binding.root.setOnClickListener {
            listener?.onTap(model.billItem, model.isSearchItem)
        }
    }
}
