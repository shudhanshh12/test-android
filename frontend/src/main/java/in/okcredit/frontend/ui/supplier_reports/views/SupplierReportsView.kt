package `in`.okcredit.frontend.ui.supplier_reports.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.SupplierreportsViewBinding
import `in`.okcredit.merchant.suppliercredit.Transaction
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SupplierReportsView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val binding: SupplierreportsViewBinding =
        SupplierreportsViewBinding.inflate(LayoutInflater.from(ctx), this, true)

    @ModelProp
    fun setTransaction(transaction: Transaction) {
        if (transaction.payment) {
            binding.payment.visible()
            binding.paymentArrow.visible()
            binding.credit.gone()
            binding.creditArrow.gone()
            binding.tvEdited.gone()
            renderPaymentDescriptionForPayment(transaction)
            CurrencyUtil.renderV2(transaction.amount, binding.payment, transaction.payment)
        } else {
            binding.payment.gone()
            binding.paymentArrow.gone()
            binding.credit.visible()
            binding.creditArrow.visible()
            binding.paymentDescription.gone()
            renderPaymentDescriptionForCredit(transaction)
            CurrencyUtil.renderV2(transaction.amount, binding.credit, transaction.payment)
        }

        binding.transactionDate.text = transaction.billDate.dayOfMonth().asText
        binding.transactionMonth.text = transaction.billDate.monthOfYear().asText.take(3)
    }

    private fun renderPaymentDescriptionForPayment(transaction: Transaction) {
        if (transaction.collectionId.isNullOrBlank().not()) {
            binding.paymentDescription.visible()
            binding.paymentDescription.text = context.getString(R.string.online_transaction)
        } else {
            binding.paymentDescription.gone()
        }
    }

    private fun renderPaymentDescriptionForCredit(transaction: Transaction) {
        if (transaction.collectionId.isNullOrBlank().not()) {
            binding.tvEdited.visible()
            binding.tvEdited.text = context.getString(R.string.online_transaction)
        } else {
            binding.tvEdited.gone()
        }
    }
}
