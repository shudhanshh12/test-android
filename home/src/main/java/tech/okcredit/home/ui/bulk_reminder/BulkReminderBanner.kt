package tech.okcredit.home.ui.bulk_reminder

import `in`.okcredit.backend.utils.CurrencyUtil
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.home.R
import tech.okcredit.home.databinding.BulkReminderBannerBinding
import tech.okcredit.home.ui.customer_tab.CustomerTabContract.BulkReminderState

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BulkReminderBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = BulkReminderBannerBinding
        .inflate(LayoutInflater.from(context), this, true)

    private var listener: BulkReminderBannerListener? = null

    init {
        binding.root.setOnClickListener {
            listener?.bulkReminderBannerClicked()
        }
        binding.notificationImageview.setOnClickListener {
            listener?.bulkReminderBellIconClicked()
        }
    }

    @ModelProp
    fun setBulkReminderState(bulkReminderState: BulkReminderState?) {
        bulkReminderState?.let {
            binding.balanceDue.text = context.getString(
                R.string.rupee_placeholder,
                CurrencyUtil.formatV2(it.totalBalanceDue)
            )
            binding.dueSince.text = context.getString(
                R.string.t_001_daily_remind_due_since,
                it.defaulterSince.toString()
            )
            if (it.showNotificationBadge) {
                binding.reminderCount.visible()
                binding.reminderCount.text = it.totalReminders.toString()
            } else {
                binding.reminderCount.gone()
            }
        }
    }

    @CallbackProp
    fun setListener(listener: BulkReminderBannerListener?) {
        this.listener = listener
    }

    interface BulkReminderBannerListener {
        fun bulkReminderBannerClicked()
        fun bulkReminderBellIconClicked()
    }
}
