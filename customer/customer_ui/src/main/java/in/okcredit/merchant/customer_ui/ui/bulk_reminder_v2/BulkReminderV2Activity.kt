package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.ActivityBulkReminderV2Binding
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.*
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.BulkReminderEpoxyModel.ReminderTab
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.analytics.BulkReminderAnalyticsImpl
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeDialog
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeDialog.ArgReminderMode
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeDialog.DefaultReminderModeListener
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderDialog
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderDialog.SendReminderDialogListener
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.BulkReminderTab.BulkReminderTabListener
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.views.TopBanner.TopBannerListener
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import dagger.android.AndroidInjection
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject
import android.content.Intent as AndroidIntent

class BulkReminderV2Activity :
    BaseActivity<State, ViewEvent, Intent>(
        label = "BulkReminderV2Activity"
    ),
    DefaultReminderModeListener,
    BulkReminderTabListener,
    SendReminderDialogListener,
    TopBannerListener {

    companion object {

        @JvmStatic
        fun getIntent(context: Context) = AndroidIntent(context, BulkReminderV2Activity::class.java)

        @JvmStatic
        fun start(context: Context) {
            val starter = getIntent(context)
            context.startActivity(starter)
        }
    }

    @Inject
    lateinit var bulkReminderV2Controller: Lazy<BulkReminderV2Controller>

    @Inject
    lateinit var tracker: Lazy<BulkReminderAnalyticsImpl>

    @Inject
    lateinit var legacyTracker: Lazy<Tracker>

    private val binding: ActivityBulkReminderV2Binding by viewLifecycleScoped(ActivityBulkReminderV2Binding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.epoxyTab.apply {
            adapter = bulkReminderV2Controller.get().adapter
        }
        binding.send.setOnClickListener {
            if (isStateInitialized() && getCurrentState().internetAvailable) {
                pushIntent(Intent.SendButtonClicked(getCurrentState().responseData))
            } else {
                shortToast(getString(R.string.t_001_daily_remind_default_reminder_type_no_internet_err))
            }
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun render(state: State) {
        bulkReminderV2Controller.get().setData(state.bulkReminderEpoxyModels)
        checkForSendButtonEnabling(state)
        checkForSelectedReminders(state)
    }

    private fun checkForSelectedReminders(state: State) {
        binding.selectedReminders.text = getString(
            R.string.t_001_daily_remind_selected_count,
            state.selectedReminderCount.toString()
        )
    }

    private fun checkForSendButtonEnabling(state: State) {
        if (state.enableSendButton) {
            binding.send.apply {
                backgroundTintList =
                    ColorStateList.valueOf(getColorFromAttr(R.attr.colorPrimary))
                isEnabled = true
            }
        } else {
            binding.send.apply {
                backgroundTintList =
                    ContextCompat.getColorStateList(this@BulkReminderV2Activity, R.color.grey400)
                isEnabled = false
            }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToSendReminderDialog -> goToSendReminderDialog(event.selectedReminders)
            is ViewEvent.TrackUpdateProfile -> trackUpdateProfile(event.accountId, event.reminderMode)
            is ViewEvent.ShowError -> {
                shortToast(event.stringRes)
            }
        }
    }

    private fun trackUpdateProfile(accountId: String, reminderMode: ReminderProfile.ReminderMode?) {
        tracker.get().trackUpdateProfile(
            accountId,
            reminderMode
        )
    }

    private fun goToSendReminderDialog(selectedReminders: List<ReminderProfile>?) {
        selectedReminders?.also {
            SendReminderDialog.showDialog(
                supportFragmentManager,
                argDispatchReminder = it,
                allReminderListedForAnalytics = getCurrentState().responseData?.reminderProfiles ?: emptyList()
            )
        }
    }

    override fun setDefaultReminderMode(customerId: String, reminderMode: ReminderProfile.ReminderMode) {
        val responseData = getCurrentState().responseData ?: return

        pushIntent(
            Intent.SetReminderMode(
                customerId,
                reminderMode,
                responseData
            )
        )
    }

    override fun onReminderTabClicked(reminderTabType: ReminderType, isCollapsed: Boolean) {
        lifecycleScope.launchWhenResumed {
            trackReminderTabClicked(reminderTabType, isCollapsed)
            pushIntent(Intent.ReminderTabClicked(reminderTabType, isCollapsed))
        }
    }

    override fun onSelectAllClicked(reminderTabType: ReminderType, defaulters: List<ReminderProfile>) {
        val responseData = getCurrentState().responseData ?: return

        tracker.get().trackReminderSelectAllClicked(
            reminderTabType = reminderTabType,
            byDefault = false
        )
        pushIntent(Intent.SelectAllClicked(responseData, reminderTabType, defaulters))
    }

    override fun onDeselectAllClicked(reminderTabType: ReminderType, defaulters: List<ReminderProfile>) {
        val responseData = getCurrentState().responseData ?: return

        tracker.get().trackReminderDeselectAllClicked(
            reminderTabType = reminderTabType
        )
        pushIntent(Intent.DeselectAllClicked(responseData, reminderTabType, defaulters))
    }

    override fun selectReminderClicked(reminderProfile: ReminderProfile) {
        val responseData = getCurrentState().responseData ?: return

        lifecycleScope.launchWhenResumed {
            tracker.get().trackReminderClicked(
                accountId = reminderProfile.customerId ?: "",
                isSelected = true,
                lastReminderSentTime = reminderProfile.lastReminderSend,
                dueINR = reminderProfile.totalBalanceDue,
                dueSinceDays = reminderProfile.dueSince,
                reminderTabType = reminderProfile.reminderType
            )
            pushIntent(Intent.ReminderSelected(responseData, reminderProfile))
        }
    }

    override fun onReminderSent(reminderProfile: ReminderProfile?) {
        if (reminderProfile == null &&
            getCurrentState().responseData == null
        ) return

        lifecycleScope.launchWhenResumed {
            pushIntent(Intent.ReminderDeselected(getCurrentState().responseData!!, reminderProfile!!))
        }
    }

    override fun deselectReminderClicked(reminderProfile: ReminderProfile) {
        val responseData = getCurrentState().responseData ?: return

        lifecycleScope.launchWhenResumed {
            tracker.get().trackReminderClicked(
                accountId = reminderProfile.customerId ?: "",
                isSelected = false,
                lastReminderSentTime = reminderProfile.lastReminderSend,
                dueINR = reminderProfile.totalBalanceDue,
                dueSinceDays = reminderProfile.dueSince,
                reminderTabType = reminderProfile.reminderType
            )
            pushIntent(Intent.ReminderDeselected(responseData, reminderProfile))
        }
    }

    override fun setReminderModeClicked(reminderProfile: ReminderProfile) {
        val customerId = reminderProfile.customerId ?: return

        tracker.get().trackReminderLongPress(customerId)
        SelectReminderModeDialog.showDialog(
            supportFragmentManager,
            argReminderMode = ArgReminderMode(
                customerId = customerId,
                reminderMode = reminderProfile.reminderMode
            )
        )
    }

    override fun onSendingReminderDismiss() {
        pushIntent(Intent.SyncLastReminderSentTime)
    }

    override fun showCongratulationsBanner() {
        pushIntent(Intent.ShowCongratulationsBanner)
    }

    private fun trackReminderTabClicked(
        reminderTabType: ReminderType,
        collapsed: Boolean,
    ) {
        val totalReminders: ReminderTab? = getCurrentState().bulkReminderEpoxyModels
            .firstOrNull {
                it is ReminderTab && it.reminderTabType == reminderTabType
            } as? ReminderTab

        tracker.get().trackTabClicked(
            reminderTabType,
            count = totalReminders?.totalDefaulters ?: 0,
            collapsed,
        )
    }

    override fun onTopBannerClicked() {
        legacyTracker.get().trackEntryPointClicked(
            source = "Banner",
            type = "Bulk Reminders List",
            target = "Payment Pending"
        )
    }
}
