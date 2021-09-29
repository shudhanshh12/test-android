package `in`.okcredit.sales_ui.ui.list_sales.views

import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.databinding.SaleDeleteLayoutBinding
import `in`.okcredit.sales_ui.utils.SalesUtil
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.utils.DateTimeUtils

class SaleDeleteLayout(context: Context?, attr: AttributeSet?) : LinearLayout(context, attr) {

    private var listener: Listener? = null
    var binding: SaleDeleteLayoutBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = SaleDeleteLayoutBinding.inflate(inflater, this, true)
        binding.root.setOnClickListener {
            listener?.onDismiss()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun setContent(sale: Models.Sale, showTime: Boolean) {
        binding.note.text = if (!sale.note.isNullOrEmpty()) sale.note else context.getString(R.string.cash_sales)
        binding.amount.text = context.getString(R.string.rupees, SalesUtil.currencyDisplayFormat(sale.amount))
        if (showTime) {
            binding.time.text = DateTimeUtils.formatTimeOnly(sale.saleDate)
        } else {
            binding.time.text = DateTimeUtils.getFormat2(context, sale.saleDate)
        }
        binding.delete.setOnClickListener {
            listener?.onDeleteClicked(sale)
        }
    }

    interface Listener {
        fun onDeleteClicked(sale: Models.Sale)
        fun onDismiss()
    }
}
