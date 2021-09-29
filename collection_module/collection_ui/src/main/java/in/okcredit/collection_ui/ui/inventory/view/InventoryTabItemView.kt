package `in`.okcredit.collection_ui.ui.inventory.view

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.InventoryEpoxyModel
import `in`.okcredit.collection.contract.InventoryItem
import `in`.okcredit.collection.contract.InventorySource
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.BillingItemViewBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.capitalizeWords
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils.formatAccountStatement
import tech.okcredit.android.base.utils.DimensionUtil

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class InventoryTabItemView @JvmOverloads constructor(
    private val ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private val binding: BillingItemViewBinding =
        BillingItemViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: Listener? = null

    private lateinit var billItem: InventoryItem

    interface Listener {
        fun clickedBill(billItem: InventoryItem)
    }

    init {
        val horizontal = DimensionUtil.dp2px(context, 16.0f).toInt()
        val vertical = DimensionUtil.dp2px(context, 12.0f).toInt()
        setPadding(horizontal, vertical, horizontal, 0)

        binding.root.setOnClickListener { listener?.clickedBill(billItem) }
    }

    @ModelProp
    fun setBillingData(item: InventoryEpoxyModel) {
        this.billItem = item.inventoryItem

        binding.textItem.text = item.inventoryItem.item.capitalizeWords()

        binding.textTime.text = formatAccountStatement(context, item.inventoryItem.createTime)

        if (item.source == InventorySource.ITEM) {
            binding.textQuantity.visible()
            binding.textAmount.text = item.inventoryItem.quantity.toString()
            binding.textItemCount.text =
                ctx.getString(R.string.inventory_rate_text, CurrencyUtil.formatV2(item.inventoryItem.price))
        } else {
            binding.textQuantity.gone()
            binding.textAmount.text =
                ctx.getString(R.string.inventory_amount_rupee, CurrencyUtil.formatV2(item.inventoryItem.price))
            binding.textItemCount.text =
                if (item.inventoryItem.quantity > 1) {
                    ctx.getString(R.string.inventory_items_number, item.inventoryItem.quantity.toString())
                } else {
                    ctx.getString(R.string.inventory_item, item.inventoryItem.quantity.toString())
                }
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }
}
