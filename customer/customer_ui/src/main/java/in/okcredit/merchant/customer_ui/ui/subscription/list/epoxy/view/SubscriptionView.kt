package `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy.view

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.request.toFormattedString
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.databinding.SubscriptionItemBinding
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy.SubscriptionItem
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.bumptech.glide.util.Preconditions
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SubscriptionView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    private var listener: ((String) -> Unit)? = null
    private val binding = SubscriptionItemBinding.inflate(LayoutInflater.from(context), this, true)
    private val simpleDateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM)

    init {
        rootView.setOnClickListener { listener?.invoke(it.tag as String) }
    }

    @ModelProp
    fun setSubscriptionItem(subscriptionItem: SubscriptionItem) {
        rootView.tag = subscriptionItem.id
        binding.apply {
            textSubscriptionName.text = subscriptionItem.name
            textStartDate.text = simpleDateFormat.format(Date(subscriptionItem.startDate * 1000))
        }
        setFrequency(subscriptionItem)
    }

    @CallbackProp
    fun setItemClickListener(listener: ((String) -> Unit)?) {
        this.listener = listener
    }

    private fun setFrequency(subscriptionItem: SubscriptionItem) {
        when (subscriptionItem.frequency) {
            SubscriptionFrequency.DAILY -> {
                binding.textFrequency.text = context.getString(R.string.repeat)
                binding.textFrequencyDetails.text = context.getString(R.string.daily)
            }
            SubscriptionFrequency.WEEKLY -> {
                binding.textFrequency.text = context.getString(R.string.repeat_weekly)
                binding.textFrequencyDetails.text = (subscriptionItem.daysInWeek)?.toFormattedString() ?: ""
            }
            SubscriptionFrequency.MONTHLY -> {
                binding.textFrequency.text = context.getString(R.string.repeat_monthly)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = subscriptionItem.startDate * 1000L
                }
                binding.textFrequencyDetails.text =
                    context.getString(
                        R.string.on_day_of_month,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH))
                    )
            }
        }
    }

    private fun getDayOfMonthSuffix(dayOfMonth: Int): String? {
        Preconditions.checkArgument(dayOfMonth in 1..31, "illegal day of month: $dayOfMonth")
        return if (dayOfMonth in 11..13) {
            "th"
        } else when (dayOfMonth % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}
