package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.SendReminderDialogBinding
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType.PENDING_REMINDER
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType.TODAYS_REMINDER
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.analytics.BulkReminderAnalyticsImpl
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.trackerData
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderContract.*
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.invisible
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.toArrayList
import tech.okcredit.android.base.utils.TextDrawableUtils
import javax.inject.Inject

class SendReminderDialog : BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>(
    "SendReminderDialog",
) {

    companion object {
        const val TAG = "SendReminderDialog"
        const val ARG_DISPATCH_REMINDER = "arg_dispatch_reminder_list"
        const val ARG_ALL_REMINDER_LISTED = "arg_all_reminder_listed"

        fun showDialog(
            fragmentManager: FragmentManager,
            argDispatchReminder: List<ReminderProfile>,
            allReminderListedForAnalytics: List<ReminderProfile>
        ) {
            val bundle = Bundle().apply {
                putParcelableArrayList(
                    ARG_DISPATCH_REMINDER, argDispatchReminder.toArrayList()
                )
                putParcelableArrayList(
                    ARG_ALL_REMINDER_LISTED, allReminderListedForAnalytics.toArrayList()
                )
            }
            SendReminderDialog().apply {
                arguments = bundle
            }.show(fragmentManager, TAG)
        }
    }

    private var listener: SendReminderDialogListener? = null
    private var lastReminderSendCount = 0

    private lateinit var binding: SendReminderDialogBinding

    @Inject
    lateinit var tracker: Lazy<BulkReminderAnalyticsImpl>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SendReminderDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener = activity as? SendReminderDialogListener

        binding.cancel.setOnClickListener {
            if (isStateInitialized() && getCurrentState().completedSendingReminders) {
                tracker.get().trackPopupDismissed()
            } else {
                trackSendReminderCancelled()
            }
            dismiss()
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun onResume() {
        super.onResume()
        provideLoadReminderIntent()
    }

    private fun provideLoadReminderIntent() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(1000L)
            val currentReminderCount = if (isStateInitialized()) getCurrentState().currentReminderSentCount else -1
            if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
                (lastReminderSendCount == currentReminderCount)
            ) {
                pushIntent(Intent.LoadReminder)
            }
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun render(state: State) {
        if (state.completedSendingReminders) {
            renderCompletedUi(state)
        } else {
            binding.pbPercentage.progress = 0
            checkForProfilePic(state.currentSendingReminder)
            checkForBalanceDue(state.currentSendingReminder)
            checkForCustomerName(state.currentSendingReminder)
            checkForDueSince(state.currentSendingReminder)
            checkForRemindersStatus(state)
        }
    }

    private fun renderCompletedUi(state: State) {
        binding.apply {
            balanceDue.gone()
            pbPercentage.invisible()
            remainingReminderStatus.gone()
            header.gone()

            binding.profileImage.setImageDrawable(getDrawableCompact(R.drawable.ic_icon_done_bg))
            profileName.text = getString(
                R.string.t_001_daily_remind_cust_sent_popup_title,
                state.totalRemindersCountToBeDispatch.toString()
            )
            binding.dueSince.text = getString(
                R.string.t_001_daily_remind_cust_sent_popup_subtitle
            )
            binding.cancel.text = getString(R.string.t_001_daily_remind_cta_ok)
        }
    }

    private fun checkForRemindersStatus(state: State) {
        binding.remainingReminderStatus.text = getString(
            R.string.t_001_daily_remind_sent_reminder_count,
            state.currentReminderSentCount.toString(),
            state.totalRemindersCountToBeDispatch.toString()
        )
    }

    private fun checkForCustomerName(currentSendingReminder: ReminderProfile?) {
        binding.profileName.text = currentSendingReminder?.customerName
    }

    private fun checkForBalanceDue(currentSendingReminder: ReminderProfile?) {
        binding.balanceDue.text = getString(
            R.string.rupee_placeholder,
            currentSendingReminder?.totalBalanceDue
        )
    }

    private fun checkForProfilePic(currentReminder: ReminderProfile?) {
        val defaultPic = TextDrawableUtils
            .getRoundTextDrawable(currentReminder?.customerName?.firstOrNull()?.toUpperCase().toString())

        if (currentReminder?.profileUrl.isNotNullOrBlank()) {
            GlideApp.with(this@SendReminderDialog)
                .load(currentReminder?.profileUrl)
                .placeholder(defaultPic)
                .circleCrop()
                .error(defaultPic)
                .fallback(defaultPic)
                .thumbnail(0.25f)
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageDrawable(defaultPic)
        }
    }

    private fun checkForDueSince(currentReminder: ReminderProfile?) {
        if (currentReminder?.dueSince == "0" || currentReminder == null) {
            binding.dueSince.text = getString(
                R.string.t_001_daily_remind_cust_due_since_error
            )
        } else {
            when {
                currentReminder.dueSince.contains("d") -> {
                    binding.dueSince.text = context?.getString(
                        R.string.t_001_daily_remind_cust_due_since,
                        currentReminder.dueSince.replace("d", "")
                    )
                }
                currentReminder.dueSince.contains("m") -> {
                    binding.dueSince.text = context?.getString(
                        R.string.t_001_daily_remind_cust_due_since_months,
                        currentReminder.dueSince.replace("m", "")
                    )
                }
                currentReminder.dueSince.contains("y") -> {
                    binding.dueSince.text = context?.getString(
                        R.string.t_001_daily_remind_cust_due_since_years,
                        currentReminder.dueSince.replace("y", "")
                    )
                }
                currentReminder.dueSince.contains("h") -> {
                    binding.dueSince.text = context?.getString(
                        R.string.t_001_daily_remind_cust_due_since_today,
                    )
                }
            }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.SendReminder -> sendReminder(event.intent)
            is ViewEvent.ShowCongratulationsBanner -> showCongratulationsBanner()
            is ViewEvent.ShowError -> {
                shortToast(getString(R.string.t_001_daily_remind_error_something_went_wong))
            }
            is ViewEvent.TrackSendingReminderCompleted -> trackSendReminderCompleted()
        }
    }

    private fun showCongratulationsBanner() {
        listener?.showCongratulationsBanner()
    }

    private fun sendReminder(intent: android.content.Intent) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(1000L)
            binding.pbPercentage.progress = 100

            listener?.onReminderSent(getCurrentState().currentSendingReminder)
            lastReminderSendCount = getCurrentState().currentReminderSentCount

            startActivity(intent)
        }
    }

    interface SendReminderDialogListener {
        fun onSendingReminderDismiss()
        fun showCongratulationsBanner()
        fun onReminderSent(reminderProfile: ReminderProfile?)
    }

    override fun onDismiss(dialog: DialogInterface) {
        listener?.onSendingReminderDismiss()
        super.onDismiss(dialog)
    }

    private fun trackSendReminderCancelled() {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = getCurrentState()
            val trackerData = state.allReminderListedForAnalytics.trackerData()
            val cancelledReminders = state.leftReminderTobeSendForAnalytics.size
            val customerCancelledReminders = state.leftReminderTobeSendForAnalytics.count {
                it.reminderType == PENDING_REMINDER
            }
            val sentTodayRemindersCancelled = state.leftReminderTobeSendForAnalytics.count {
                it.reminderType == TODAYS_REMINDER
            }

            val reminderSent = trackerData.reminderSelected - cancelledReminders
            val customerReminderSent = trackerData.customerReminderSelected - customerCancelledReminders
            val sentTodayReminderSent = trackerData.sendTodayReminderSelected - sentTodayRemindersCancelled

            tracker.get().trackSendingReminderCancelled(
                totalReminder = trackerData.totalReminder,
                customerReminderListed = trackerData.customerReminderListed,
                sendTodayReminderListed = trackerData.sendTodayReminderListed,
                reminderSelected = trackerData.reminderSelected,
                customerReminderSelected = trackerData.customerReminderSelected,
                sendTodayReminderSelected = trackerData.sendTodayReminderSelected,
                customerSelectAll = trackerData.customerSelectAll,
                sendTodaySelectAll = trackerData.sendTodaySelectAll,
                reminderCancelled = cancelledReminders,
                customerReminderCancelled = customerCancelledReminders,
                sendTodayReminderCancelled = sentTodayRemindersCancelled,
                reminderSent = reminderSent,
                customerReminderSent = customerReminderSent,
                sentTodayReminderSent = sentTodayReminderSent
            )
        }
    }

    private fun trackSendReminderCompleted() {
        viewLifecycleOwner.lifecycleScope.launch {
            val state = getCurrentState()
            val trackerData = state.allReminderListedForAnalytics.trackerData()

            tracker.get().trackBulkReminderSendCompleted(
                totalReminder = trackerData.totalReminder,
                customerReminderListed = trackerData.customerReminderListed,
                sendTodayReminderListed = trackerData.sendTodayReminderListed,
                reminderSelected = trackerData.reminderSelected,
                customerReminderSelected = trackerData.customerReminderSelected,
                sendTodayReminderSelected = trackerData.sendTodayReminderSelected,
                customerSelectAll = trackerData.customerSelectAll,
                sendTodaySelectAll = trackerData.sendTodaySelectAll,
            )
        }
    }
}
