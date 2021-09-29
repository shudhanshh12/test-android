package `in`.okcredit.supplier.statement.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.suppliercredit.Transaction
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import merchant.okcredit.accounting.databinding.AccountStatementTxItemBinding
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.DimensionUtil
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SupplierTransactionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var supplierTransaction: Transaction

    interface Listener {
        fun onTransactionClicked(transaction: Transaction)
    }

    private val binding = AccountStatementTxItemBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setTransaction(transaction: Transaction) {
        this.supplierTransaction = transaction
        val params = binding.txContainer.layoutParams as FrameLayout.LayoutParams

        if (transaction.payment) {
            params.gravity = Gravity.END
        } else {
            params.gravity = Gravity.START
        }
        binding.txContainer.layoutParams = params
        binding.txContainer.requestLayout()

        CurrencyUtil.renderV2(transaction.amount, binding.txAmount, transaction.payment)
        binding.txDate.text = DateTimeUtils.formatAccountStatement(context, transaction.billDate)

        if (transaction.isOnlineTransaction()) {
            binding.customerName.setPadding(
                DimensionUtil.dp2px(context, 16F).toInt(),
                0,
                DimensionUtil.dp2px(context, 16F).toInt(),
                0
            )
            binding.onlinePayment.visibility = View.VISIBLE
        } else {
            binding.onlinePayment.visibility = View.GONE
            binding.customerName.setPadding(
                DimensionUtil.dp2px(context, 16F).toInt(), DimensionUtil.dp2px(context, 10F).toInt(),
                DimensionUtil.dp2px(context, 16F).toInt(), 0
            )
        }
    }

    @ModelProp
    fun setName(name: String?) {
        binding.customerName.apply {
            text = name
            isVisible = name.isNotNullOrBlank()
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        rootView.clicks()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onTransactionClicked(supplierTransaction) }
            .subscribe()
    }
}
