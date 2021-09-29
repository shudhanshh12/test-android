package `in`.okcredit.merchant.customer_ui.ui.customer.views

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.CustomerAcknowledgeActionViewBinding
import `in`.okcredit.merchant.customer_ui.ui.customer.CustomerScreenItem
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.debounceClickListener
import tech.okcredit.android.base.extensions.getString

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AcknowledgeActionView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attributeSet, defStyle) {

    private val binding = CustomerAcknowledgeActionViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var type: Int = 0
    private var listener: AcknowledgeActionListener? = null

    init {
        binding.root.debounceClickListener {
            listener?.onActionAcknowledgeClicked(type)
        }
    }

    @ModelProp
    fun setAcknowledgeActionItem(item: CustomerScreenItem.AcknowledgeActionItem) {
        this.type = item.type

        setMessageBasedOnType(item.type)
    }

    private fun setMessageBasedOnType(type: Int) {
        if (type == 0) {
            binding.textMessage.text = buildSpannedString {
                append(getString(R.string.message_bank_details_added))
                append(" ")
                color(ContextCompat.getColor(context, R.color.indigo_primary)) {
                    append(getString(R.string.sending_reminder))
                }
            }
        }
    }

    @CallbackProp
    fun setListener(listener: AcknowledgeActionListener?) {
        this.listener = listener
    }

    interface AcknowledgeActionListener {
        fun onActionAcknowledgeClicked(type: Int)
    }
}
