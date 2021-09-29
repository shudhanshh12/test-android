package `in`.okcredit.merchant.customer_ui.ui.customerreports.views

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.customerreports_view.view.*
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.invisible
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class CustomerReportsView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0
) : FrameLayout(ctx, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(ctx).inflate(R.layout.customerreports_view, this, true)
    }

    @ModelProp
    fun setTransaction(transaction: Transaction) {
        when {
            transaction.transactionCategory == Transaction.DISCOUNT -> {
                payment.visible()
                payment_arrow.gone()
                credit.gone()
                credit_arrow.gone()
                tv_edited.gone()
                payment_description.visible()
                payment.setTextColor(ContextCompat.getColor(context, R.color.grey900))
                payment_description.text = context.getString(R.string.discount)
                payment.text = StringBuilder(context.getString(R.string.rupee_symbol))
                    .append(CurrencyUtil.formatV2(transaction.amountV2))
            }
            transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN -> {
                payment.visible()
                payment_arrow.visible()
                credit.gone()
                credit_arrow.gone()
                tv_edited.gone()
                renderPaymentDescriptionForPayment(transaction)
                CurrencyUtil.renderV2(transaction.amountV2, payment, transaction.type)
            }
            transaction.type == Transaction.CREDIT -> {
                payment.gone()
                payment_arrow.gone()
                credit.visible()
                credit_arrow.visible()
                payment_description.invisible()
                renderPaymentDescriptionForCredit(transaction)
                CurrencyUtil.renderV2(transaction.amountV2, credit, transaction.type)
            }
        }

        transaction_date.text = transaction.billDate.dayOfMonth().asText
        transaction_month.text = transaction.billDate.monthOfYear().asText.take(3)
    }

    private fun renderPaymentDescriptionForCredit(transaction: Transaction) {
        when {
            transaction.amountUpdated -> {
                tv_edited.text =
                    context.getString(R.string.edited_on, DateTimeUtils.formatDateOnly(transaction.amountUpdatedAt))
                tv_edited.visible()
            }
            transaction.isOnlinePaymentTransaction -> {
                tv_edited.visible()
                tv_edited.text = context.getString(R.string.online_transaction)
            }
            transaction.isSubscriptionTransaction -> {
                tv_edited.visible()
                tv_edited.text = context.getString(R.string.subscription)
            }
            else -> {
                tv_edited.gone()
            }
        }
    }

    private fun renderPaymentDescriptionForPayment(transaction: Transaction) {
        when {
            transaction.amountUpdated -> {
                payment_description.visible()
                payment_description.text =
                    context.getString(R.string.edited_on, DateTimeUtils.formatDateOnly(transaction.amountUpdatedAt))
            }
            transaction.isOnlinePaymentTransaction -> {
                payment_description.visible()
                payment_description.text = context.getString(R.string.online_transaction)
            }
            transaction.isSubscriptionTransaction -> {
                payment_description.visible()
                payment_description.text = context.getString(R.string.subscription)
            }
            else -> {
                payment_description.invisible()
            }
        }
    }
}
