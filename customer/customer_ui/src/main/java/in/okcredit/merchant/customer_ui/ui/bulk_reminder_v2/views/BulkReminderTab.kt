package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.BulkReminderTabBinding
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.BulkReminderEpoxyModel.ReminderTab
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType.PENDING_REMINDER
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulk_reminder_tab.BulkReminderTabContract.Intent
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.bulk_reminder_tab.BulkReminderTabContract.State
import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import javax.inject.Inject

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BulkReminderTab @JvmOverloads constructor(
    ctx: Context,
    val attrs: AttributeSet? = null,
    val defStyleAttr: Int = 0,
) : BaseLayout<State>(ctx, attrs, defStyleAttr),
    ReminderItemView.ReminderItemViewListener {

    private val binding = BulkReminderTabBinding.inflate(LayoutInflater.from(context), this, true)

    private var listener: BulkReminderTabListener? = null
    private var state: ReminderTab? = null

    @Inject
    lateinit var reminderController: Lazy<ReminderController>

    init {
        binding.clickableArea.setOnClickListener {
            state?.also { state ->
                state.reminderTabType?.also {
                    listener?.onReminderTabClicked(it, !state.isCollapsed)
                }
            }
        }
        binding.cbSelectAll.setOnClickListener {
            state?.also { state ->
                if (state.isAllSelected) {
                    state.reminderTabType?.also {
                        listener?.onDeselectAllClicked(it, state.defaulters)
                    }
                } else {
                    state.reminderTabType?.also {
                        listener?.onSelectAllClicked(it, state.defaulters)
                    }
                }
            }
        }
        binding.selectAll.setOnClickListener {
            state?.also { state ->
                if (state.isAllSelected) {
                    state.reminderTabType?.also {
                        listener?.onDeselectAllClicked(it, state.defaulters)
                    }
                } else {
                    state.reminderTabType?.also {
                        listener?.onSelectAllClicked(it, state.defaulters)
                    }
                }
            }
        }
        binding.reminderProfilesList.apply {
            adapter = reminderController.get().adapter
        }
    }

    @ModelProp
    fun setReminderTab(state: ReminderTab?) {
        this.state = state
        state?.also {
            render(state)
        }
    }

    private fun render(state: ReminderTab) {
        reminderController.get().setData(state.defaulters)
        checkForReminderTabType(state)
        checkForTabCollapsed(state)
        checkForSelectedAll(state)
    }

    private fun checkForSelectedAll(state: ReminderTab) {
        if (state.isAllSelected) {
            binding.cbSelectAll.setImageResource(R.drawable.ic_tick_green)
        } else {
            binding.cbSelectAll.setImageResource(R.drawable.circle_filled_white_grey_stroke)
        }
    }

    private fun checkForTabCollapsed(state: ReminderTab) {
        if (state.isCollapsed) {
            binding.reminderProfilesList.gone()
            binding.arrow.setImageResource(R.drawable.ic_arrow_down_green_small)
        } else {
            binding.reminderProfilesList.visible()
            binding.arrow.setImageResource(R.drawable.ic_arrow_up_green)
        }
    }

    private fun checkForReminderTabType(state: ReminderTab) {
        if (state.reminderTabType == PENDING_REMINDER) {
            binding.headingTop.text = context.getString(
                R.string.t_001_daily_remind_accordian_cust,
                state.totalDefaulters.toString()
            )
        } else {
            binding.headingTop.text = context.getString(
                R.string.t_001_daily_remind_accordian_sent_today,
                state.totalDefaulters.toString()
            )
        }
    }

    interface BulkReminderTabListener {
        fun onReminderTabClicked(reminderTabType: ReminderType, isCollapsed: Boolean)
        fun onSelectAllClicked(reminderTabType: ReminderType, defaulters: List<ReminderProfile>)
        fun onDeselectAllClicked(reminderTabType: ReminderType, defaulters: List<ReminderProfile>)
        fun selectReminderClicked(reminderProfile: ReminderProfile)
        fun deselectReminderClicked(reminderProfile: ReminderProfile)
        fun setReminderModeClicked(reminderProfile: ReminderProfile)
    }

    @CallbackProp
    fun setListener(listener: BulkReminderTabListener?) {
        this.listener = listener
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun render(state: State) {}

    override fun selectReminderClicked(reminderProfile: ReminderProfile) {
        listener?.selectReminderClicked(reminderProfile)
    }

    override fun deselectReminderClicked(reminderProfile: ReminderProfile) {
        listener?.deselectReminderClicked(reminderProfile)
    }

    override fun setReminderMode(reminderProfile: ReminderProfile) {
        listener?.setReminderModeClicked(reminderProfile)
    }
}
