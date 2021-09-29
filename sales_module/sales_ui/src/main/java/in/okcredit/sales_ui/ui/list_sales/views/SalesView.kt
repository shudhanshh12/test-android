package `in`.okcredit.sales_ui.ui.list_sales.views

import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.databinding.ItemSaleBinding
import `in`.okcredit.sales_ui.utils.SalesUtil
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.utils.DateTimeUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SalesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var listener: Listener? = null

    private var binding: ItemSaleBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = ItemSaleBinding.inflate(inflater, this, true)
    }

    @ModelProp
    fun setSale(state: SalesController.SalesViewState) {
        binding.amount.text = context.getString(R.string.rupees, SalesUtil.currencyDisplayFormat(state.sale.amount))
        binding.note.text =
            if (state.sale.note.isNullOrEmpty().not()) state.sale.note else getString(R.string.cash_sales)

        if (state.showTime) {
            binding.time.text = DateTimeUtils.formatTimeOnly(state.sale.saleDate)
        } else {
            binding.time.text = DateTimeUtils.getFormat2(context, state.sale.saleDate)
        }

        if (state.sale.deletedAt != null) {
            binding.deleted.visibility = View.VISIBLE
            binding.amount.background = context.getDrawable(R.drawable.strike_through)
            binding.amount.setTextColor(context.resources.getColor(R.color.grey800))
            binding.note.setTextColor(context.resources.getColor(R.color.grey800))
            binding.time.setTextColor(context.resources.getColor(R.color.grey800))
        } else {
            binding.deleted.visibility = View.GONE
            binding.amount.background = null
            binding.amount.setTextColor(context.resources.getColor(R.color.grey900))
            binding.note.setTextColor(context.resources.getColor(R.color.grey900))
            binding.time.setTextColor(context.resources.getColor(R.color.grey900))
        }

        binding.root.setOnClickListener {
            if (state.sale.deletedAt == null) {
                listener?.onClick(state.sale)
            }
        }
        binding.root.setOnLongClickListener {
            if (state.sale.deletedAt == null) {
                listener?.onLongClick(state.sale)
            }
            return@setOnLongClickListener true
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface Listener {
        fun onLongClick(sale: Models.Sale)
        fun onClick(sale: Models.Sale)
    }
}
