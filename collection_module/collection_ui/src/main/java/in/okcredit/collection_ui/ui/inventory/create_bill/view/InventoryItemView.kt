package `in`.okcredit.collection_ui.ui.inventory.create_bill.view

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.ItemCreateBillBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.capitalizeWords
import tech.okcredit.android.base.extensions.getDrawableCompact

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class InventoryItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: ItemCreateBillBinding
    private lateinit var inventoryItem: InventoryItem

    private var listener: Listener? = null

    interface Listener {
        fun onAdd(billItem: InventoryItem)
        fun onDelete(billItem: InventoryItem)
    }

    init {
        val inflater = LayoutInflater.from(context)
        binding = ItemCreateBillBinding.inflate(inflater, this, true)

        binding.textPlus.setOnClickListener {
            listener?.onAdd(inventoryItem.copy(quantity = 1))
        }

        binding.textAdd.setOnClickListener {
            listener?.onAdd(inventoryItem.copy(quantity = 1))
        }

        binding.textMinus.setOnClickListener {
            listener?.onDelete(inventoryItem)
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    @ModelProp
    fun setBillItem(item: InventoryItem) {
        inventoryItem = item
        binding.textName.text = item.item.capitalizeWords()
        binding.rate.text = context.getString(R.string.inventory_rate_text, CurrencyUtil.formatV2(item.price))
        if (item.quantity > 0) {
            binding.clQuantityLayout.background =
                context.getDrawableCompact(R.drawable.circle_background_green_primary_outline)
            binding.textMinus.visibility = View.VISIBLE
            binding.textQuantity.visibility = View.VISIBLE
            binding.textAdd.visibility = View.GONE
            binding.textQuantity.text = item.quantity.toString()
            if (item.quantity > 1) {
                binding.textMinus.setImageResource(R.drawable.ic_minus)
                binding.textMinus.imageTintList = ContextCompat.getColorStateList(context, R.color.green_primary)
            } else {
                binding.textMinus.setImageResource(R.drawable.ic_delete_outline)
                binding.textMinus.imageTintList = ContextCompat.getColorStateList(context, R.color.grey400)
            }
        } else {
            binding.clQuantityLayout.background =
                context.getDrawableCompact(R.drawable.circle_background_grey100_outline)
            binding.textMinus.visibility = View.GONE
            binding.textQuantity.visibility = View.GONE
            binding.textAdd.visibility = View.VISIBLE
        }
    }
}
