package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.di

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderContract
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderDialog
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderDialog.Companion.ARG_ALL_REMINDER_LISTED
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderDialog.Companion.ARG_DISPATCH_REMINDER
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.send_reminder_dialog.SendReminderViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import java.lang.IllegalStateException
import javax.inject.Provider

@Module
abstract class SendReminderModule {

    companion object {

        @Provides
        fun initialState(fragment: SendReminderDialog): SendReminderContract.State {
            val dispatchReminders =
                fragment.arguments?.getParcelableArrayList<ReminderProfile>(ARG_DISPATCH_REMINDER)
                    as? ArrayList<ReminderProfile>

            val allReminderProfilesForAnalytics =
                fragment.arguments?.getParcelableArrayList<ReminderProfile>(ARG_ALL_REMINDER_LISTED)
                    as? ArrayList<ReminderProfile>
            return SendReminderContract.State(
                remindersTobeSend = dispatchReminders?.toList() ?: throw IllegalStateException("No Reminders Found"),
                currentSendingReminder = dispatchReminders[0], // first Reminder
                allReminderListedForAnalytics = allReminderProfilesForAnalytics?.toList() ?: emptyList(),
                leftReminderTobeSendForAnalytics = dispatchReminders.toList(),
                totalRemindersCountToBeDispatch = dispatchReminders.size
            )
        }

        @Provides
        fun viewModel(
            fragment: SendReminderDialog,
            viewModelProvider: Provider<SendReminderViewModel>,
        ): MviViewModel<SendReminderContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
