package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ViewCollectionNudgeCustomerBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerScreenItem
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import merchant.okcredit.accounting.utils.AccountingSharedUtils.TxnGravity
import tech.okcredit.android.base.extensions.debounceClickListener
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DimensionUtil

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class InfoNudgeView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(ctx, attrs, defStyleAttr) {

    private val binding = ViewCollectionNudgeCustomerBinding.inflate(LayoutInflater.from(ctx), this)

    private var mListener: Listener? = null
    private var type: Int = 0

    init {
        val padding = DimensionUtil.dp2px(context, 16f).toInt()
        setPadding(padding, padding, padding, padding)

        binding.root.debounceClickListener {
            mListener?.onInfoNudgeClicked(type)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mListener = null
    }

    interface Listener {
        fun onInfoNudgeClicked(type: Int)
    }

    @ModelProp
    fun setInfoNudgeItem(item: CustomerScreenItem.InfoNudgeItem) {
        type = item.type
        if (item.gravity == TxnGravity.LEFT) {
            showPaymentSide()
        } else {
            showCreditSide()
        }
        setMessageBasedOnType(item)
        setIcon(item)
    }

    private fun setIcon(item: CustomerScreenItem.InfoNudgeItem) {
        binding.apply {
            if (type in listOf(1, 2, 3, 4)) {
                if (item.gravity == TxnGravity.LEFT) {
                    binding.paymentSide.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gift_box_green, 0, 0, 0)
                } else {
                    binding.creditSide.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_gift_box_green, 0, 0, 0)
                }
            }
        }
    }

    private fun setMessageBasedOnType(item: CustomerScreenItem.InfoNudgeItem) {
        // for default 0 value
        var message = getString(R.string.real_time_contextual_message_card_text_credit_se)
        var clickableString = getString(R.string.know_more)

        when (item.type) {
            0 -> {
                message = getString(R.string.real_time_contextual_message_card_text_credit_se)
                clickableString = getString(R.string.know_more)
            }
            1 -> {
                message = getString(R.string.ref_online_payment_customerlist_background)
                clickableString = getString(R.string.ref_online_payment_customerlist_cta_1)
            }
            2 -> {
                message = getString(R.string.ref_online_payment_customerlist_text_2)
                clickableString = getString(R.string.ref_online_payment_customerlist_cta_3)
            }
            3 -> {
                message = getString(R.string.ref_online_payment_customerlist_text_3)
                clickableString = getString(R.string.ref_online_payment_customerlist_cta_3)
            }
            4 -> {
                message = if (item.customerName.isNotNullOrBlank()) context.getString(
                    R.string.ref_online_payment_customerlist_text_4,
                    item.customerName
                ) else getString(R.string.ref_online_payment_customerlist_text_4)
                clickableString = getString(R.string.ref_online_payment_customerlist_cta_4)
            }
            5 -> {
                message = getString(R.string.real_time_contextual_message_card_text_payment_se)
                clickableString = getString(R.string.know_more)
            }
        }

        val spannable = buildSpannedString {
            append(message)
            append(" ")
            color(context.getColorCompat(R.color.indigo_primary)) {
                append(clickableString)
            }
        }

        if (item.gravity == TxnGravity.LEFT) {
            binding.paymentSide.text = spannable
        } else {
            binding.creditSide.text = spannable
        }
    }

    private fun showPaymentSide() {
        binding.paymentSide.visible()
        binding.creditSide.gone()
    }

    private fun showCreditSide() {
        binding.paymentSide.gone()
        binding.creditSide.visible()
    }

    @CallbackProp
    fun setListener(listener: Listener?) {
        mListener = listener
    }
}
