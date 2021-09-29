package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ReminderItemViewBinding
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.TextDrawableUtils

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ReminderItemView @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : FrameLayout(ctx, attrs, defStyleAttr) {

    private val binding = ReminderItemViewBinding
        .inflate(LayoutInflater.from(context), this, true)

    private var listener: ReminderItemViewListener? = null
    private var state: ReminderProfile? = null

    init {
        binding.root.setOnClickListener {
            state?.also {
                if (it.isSelected) {
                    listener?.deselectReminderClicked(it)
                } else {
                    listener?.selectReminderClicked(it)
                }
            }
        }
        binding.cbSelect.setOnClickListener {
            state?.also {
                if (it.isSelected) {
                    listener?.deselectReminderClicked(it)
                } else {
                    listener?.selectReminderClicked(it)
                }
            }
        }
        binding.reminderMode.setOnClickListener {
            state?.also {
                listener?.setReminderMode(it)
            }
        }
    }

    @ModelProp
    fun setReminderProfile(state: ReminderProfile) {
        this.state = state
        checkForCustomerProfilePic(state)
        checkForCustomerName(state)
        checkForDueSince(state)
        checkForBalanceDue(state)
        checkForLastReminderSentTime(state)
        checkForReminderMode(state)
        checkForSelected(state)
    }

    @ModelProp
    fun isLastReminderProfile(lastModel: Boolean) {
        if (lastModel) {
            binding.bottomDivider.gone()
        } else {
            binding.bottomDivider.visible()
        }
    }

    private fun checkForSelected(state: ReminderProfile) {
        binding.apply {
            if (state.isSelected) {
                binding.cbSelect.setImageResource(R.drawable.ic_tick_green)
            } else {
                binding.cbSelect.setImageResource(R.drawable.circle_filled_white_grey_stroke)
            }
        }
    }

    private fun checkForReminderMode(state: ReminderProfile) {
        binding.apply {
            if (state.reminderMode == ReminderProfile.ReminderMode.SMS) {
                reminderMode.text = context.getString(R.string.t_001_daily_remind_default_reminder_type_sms)
                reminderMode.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_sms_outline,
                    0,
                    R.drawable.ic_arrow_down_black,
                    0
                )
            } else {
                reminderMode.text = context.getString(R.string.t_001_daily_remind_default_reminder_type_wa)
                reminderMode.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_icon_whatsapp,
                    0,
                    R.drawable.ic_arrow_down_black,
                    0
                )
            }
        }
    }

    private fun checkForLastReminderSentTime(state: ReminderProfile) {
        if (state.lastReminderSend == "0") {
            binding.lastReminderSendTime.text = context.getString(
                R.string.t_001_daily_remind_last_reminder_never
            )
        } else {
            when {
                state.lastReminderSend.endsWith("d") -> {
                    binding.lastReminderSendTime.text = context.getString(
                        R.string.t_001_daily_remind_last_reminder,
                        state.lastReminderSend.replace("d", "")
                    )
                }
                state.lastReminderSend.endsWith("m") -> {
                    binding.lastReminderSendTime.text = context.getString(
                        R.string.t_001_daily_remind_last_reminder,
                        state.lastReminderSend.replace("m", "")
                    )
                }
                state.lastReminderSend.endsWith("y") -> {
                    binding.lastReminderSendTime.text = context.getString(
                        R.string.t_001_daily_remind_last_reminder,
                        state.lastReminderSend.replace("y", "")
                    )
                }
                state.lastReminderSend.endsWith("h") -> {
                    binding.lastReminderSendTime.text = context.getString(
                        R.string.t_001_daily_remind_last_reminder_today,
                    )
                }
            }
        }
    }

    private fun checkForBalanceDue(state: ReminderProfile) {
        binding.balanceDue.text = context.getString(
            R.string.rupee_placeholder,
            state.totalBalanceDue
        )
    }

    private fun checkForDueSince(state: ReminderProfile) {
        if (state.dueSince != "0") {
            when {
                state.dueSince.contains("d") -> {
                    binding.dueSince.text = context.getString(
                        R.string.t_001_daily_remind_cust_due_since,
                        state.dueSince.replace("d", "")
                    )
                }
                state.dueSince.contains("m") -> {
                    binding.dueSince.text = context.getString(
                        R.string.t_001_daily_remind_cust_due_since_months,
                        state.dueSince.replace("m", "")
                    )
                }
                state.dueSince.contains("y") -> {
                    binding.dueSince.text = context.getString(
                        R.string.t_001_daily_remind_cust_due_since_years,
                        state.dueSince.replace("y", "")
                    )
                }
                state.dueSince.contains("h") -> {
                    binding.dueSince.text = context.getString(
                        R.string.t_001_daily_remind_cust_due_since_today,
                    )
                }
            }
        } else {
            binding.dueSince.gone()
        }
    }

    private fun checkForCustomerName(state: ReminderProfile) {
        binding.customerName.text = state.customerName
    }

    private fun checkForCustomerProfilePic(state: ReminderProfile) {
        val defaultPic = TextDrawableUtils
            .getRoundTextDrawable(state.customerName?.firstOrNull()?.toUpperCase().toString())

        if (state.profileUrl.isNotNullOrBlank()) {
            GlideApp.with(context)
                .load(state.profileUrl)
                .placeholder(defaultPic)
                .circleCrop()
                .error(defaultPic)
                .fallback(defaultPic)
                .thumbnail(0.25f)
                .into(binding.customerProfile)
        } else {
            binding.customerProfile.setImageDrawable(defaultPic)
        }
    }

    @CallbackProp
    fun setListener(listener: ReminderItemViewListener?) {
        this.listener = listener
    }

    interface ReminderItemViewListener {
        fun selectReminderClicked(reminderProfile: ReminderProfile)
        fun deselectReminderClicked(reminderProfile: ReminderProfile)
        fun setReminderMode(reminderProfile: ReminderProfile)
    }
}
