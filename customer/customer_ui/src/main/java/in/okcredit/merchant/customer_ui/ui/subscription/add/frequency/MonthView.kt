package `in`.okcredit.merchant.customer_ui.ui.subscription.add.frequency

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.FrequencyMonthViewBinding
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.getColorCompat

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class MonthView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private var listener: ((Int) -> Unit)? = null
    private val binding = FrequencyMonthViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.textDate.setOnClickListener {
            val date = binding.textDate.text.toString().toInt()
            listener?.invoke(date)
        }
    }

    @CallbackProp
    fun setClickListener(listener: ((Int) -> Unit)?) {
        this.listener = listener
    }

    @ModelProp
    fun setDate(date: Int) {
        binding.textDate.text = date.toString()
    }

    @ModelProp
    fun setChecked(checked: Boolean) {
        if (checked) {
            binding.textDate.setBackgroundResource(R.drawable.circle_shape_green_filled)
            binding.textDate.setTextColor(Color.WHITE)
        } else {
            binding.textDate.setTextColor(context.getColorCompat(R.color.grey900))
            binding.textDate.background = null
        }
    }
}
