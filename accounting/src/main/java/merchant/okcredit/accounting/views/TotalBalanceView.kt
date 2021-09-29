package merchant.okcredit.accounting.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.accounting.R
import merchant.okcredit.accounting.databinding.AccountStatementSummaryBinding
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.extensions.getColorFromAttr

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TotalBalanceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = AccountStatementSummaryBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setTotal(totalAmount: Long) {
        if (totalAmount > 0) {
            TempCurrencyUtil.renderV2(totalAmount, binding.total, false)
            binding.totalLabel.text = context.resources.getString(R.string.due)
        } else {
            TempCurrencyUtil.renderV2(totalAmount, binding.total, true)
            binding.total.setTextColor(context!!.getColorFromAttr(R.attr.colorPrimary))
            binding.totalLabel.text = context.resources.getString(R.string.advance)
        }
    }

    @ModelProp
    fun setPaymentAmount(paymentAmount: Long) {
        binding.paymentTotal.text = String.format("₹%s", TempCurrencyUtil.formatV2(paymentAmount))
    }

    @ModelProp
    fun setPaymentCount(count: Int) {
        binding.paymentCount.text = resources.getQuantityString(R.plurals.payment_plural, count, count)
    }

    @ModelProp
    fun setCreditAmount(creditAmount: Long) {
        binding.creditTotal.text = String.format("₹%s", TempCurrencyUtil.formatV2(creditAmount))
    }

    @ModelProp
    fun setDiscountAmount(discountAmount: Long) {
        binding.discountAmount.text = String.format("₹%s", TempCurrencyUtil.formatV2(discountAmount))
    }

    @ModelProp
    fun setCreditCount(count: Int) {
        binding.creditCount.text = resources.getQuantityString(R.plurals.credit_plural, count, count)
    }
}
