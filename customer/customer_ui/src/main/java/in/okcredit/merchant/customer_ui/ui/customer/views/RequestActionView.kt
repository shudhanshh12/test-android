package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.RequestMerchantInfoViewBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerScreenItem
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.debounceClickListener
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible

/**
 * This card should used whenever we want to request any info from the merchant or want him to perform any action.
 * It contains a message and a CTA to perform the action
 */
@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class RequestActionView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = RequestMerchantInfoViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: RequestActionListener? = null
    private var type: Int = 0

    init {
        binding.buttonAction.debounceClickListener {
            listener?.onRequestActionClicked(type)
        }
    }

    override fun onDetachedFromWindow() {
        listener = null
        super.onDetachedFromWindow()
    }

    @ModelProp
    fun setRequestActionItem(item: CustomerScreenItem.RequestActionItem) {
        when (item.type) {
            0 -> {
                val name = item.customerName ?: getString(R.string.customer)
                binding.imageStart.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_icon_bank
                    )
                )
                binding.imageEnd.gone()
                binding.imageStart.visible()
                binding.textMessage.text = context.getString(R.string.customer_add_bank_request_single, name)
                binding.textAction.text = context.getString(R.string.add_bank_details)
            }
            1 -> {
                binding.textMessage.text = context.getString(R.string.real_time_contextual_message_card_text_credit_se)
                binding.imageEnd.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_chevron_right_black_24_dp
                    )
                )
                binding.imageEnd.visible()
                binding.imageStart.gone()
                binding.textAction.text =
                    context.getString(R.string.real_time_contextual_message_card_text_credit_cta_se)
                if (this.type != item.type) {
                    AnimationUtils.shakeV2(binding.imageEnd)
                }
            }
            2 -> {
                binding.imageEnd.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_chevron_right_black_24_dp
                    )
                )
                binding.imageEnd.visible()
                binding.imageStart.gone()
                binding.textMessage.text = context.getString(R.string.real_time_contextual_message_card_text_payment_se)
                binding.textAction.text =
                    context.getString(R.string.real_time_contextual_message_card_text_payment_cta_se)
                if (this.type != item.type) {
                    AnimationUtils.shakeV2(binding.imageEnd)
                }
            }
            else -> {
                binding.imageStart.gone()
                binding.imageEnd.gone()
                binding.textMessage.text = ""
                binding.textAction.text = ""
            }
        }
        this.type = item.type
    }

    @CallbackProp
    fun setListener(listener: RequestActionListener?) {
        this.listener = listener
    }

    interface RequestActionListener {
        fun onRequestActionClicked(type: Int)
    }
}
