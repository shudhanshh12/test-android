package `in`.okcredit.frontend.ui.account_statement.views

import `in`.okcredit.backend._offline.model.TransactionWrapper
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.frontend.R
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.jakewharton.rxbinding3.view.clicks
import merchant.okcredit.accounting.databinding.AccountStatementTxItemBinding
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.DimensionUtil
import java.util.concurrent.TimeUnit

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TransactionView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private lateinit var transaction: TransactionWrapper

    interface Listener {
        fun onTransactionClicked(txn: Transaction)
    }

    private val binding = AccountStatementTxItemBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setTransaction(transaction: TransactionWrapper) {
        this.transaction = transaction
        val params = binding.txContainer.layoutParams as LayoutParams

        if (transaction.transaction.type == Transaction.PAYMENT || transaction.transaction.type == Transaction.RETURN) {
            params.gravity = Gravity.START
        } else if (transaction.transaction.type == Transaction.CREDIT) {
            params.gravity = Gravity.END
        }
        binding.txContainer.layoutParams = params
        binding.txContainer.requestLayout()

        CurrencyUtil.renderV2(transaction.transaction.amountV2, binding.txAmount, transaction.transaction.type)
        binding.txDate.text = DateTimeUtils.formatAccountStatement(context, transaction.transaction.billDate)

        if (transaction.customerName.isNullOrEmpty()) {
            binding.customerName.visibility = View.GONE
        } else {
            binding.customerName.visibility = View.VISIBLE
            binding.customerName.text = transaction.customerName
        }

        when {
            transaction.transaction.isOnlinePaymentTransaction -> {
                binding.customerName.setPadding(
                    DimensionUtil.dp2px(context, 16F).toInt(),
                    0,
                    DimensionUtil.dp2px(context, 16F).toInt(),
                    0
                )
                binding.onlinePayment.text = context.getString(R.string.online_payment)
                binding.onlinePayment.visibility = View.VISIBLE
            }
            transaction.transaction.isSubscriptionTransaction -> {
                binding.customerName.setPadding(
                    DimensionUtil.dp2px(context, 16F).toInt(),
                    0,
                    DimensionUtil.dp2px(context, 16F).toInt(),
                    0
                )
                binding.onlinePayment.text = context.getString(R.string.subscription)
                binding.onlinePayment.visibility = View.VISIBLE
            }
            transaction.transaction.transactionCategory == Transaction.DISCOUNT -> {
                binding.customerName.setPadding(
                    DimensionUtil.dp2px(context, 16F).toInt(),
                    0,
                    DimensionUtil.dp2px(context, 16F).toInt(),
                    0
                )
                binding.onlinePayment.visibility = View.VISIBLE
                binding.onlinePayment.text = context.getString(R.string.discount_offered)
            }
            transaction.transaction.amountUpdated -> {
                binding.customerName.setPadding(
                    DimensionUtil.dp2px(context, 16F).toInt(),
                    0,
                    DimensionUtil.dp2px(context, 16F).toInt(),
                    0
                )
                binding.onlinePayment.visibility = View.VISIBLE
                binding.onlinePayment.text = context.getString(R.string.edited)
            }
            else -> {
                binding.onlinePayment.visibility = View.GONE
                binding.customerName.setPadding(
                    DimensionUtil.dp2px(context, 16F).toInt(), DimensionUtil.dp2px(context, 10F).toInt(),
                    DimensionUtil.dp2px(context, 16F).toInt(), 0
                )
            }
        }
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        rootView.clicks()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .doOnNext { listener?.onTransactionClicked(transaction.transaction) }
            .subscribe()
    }
}
