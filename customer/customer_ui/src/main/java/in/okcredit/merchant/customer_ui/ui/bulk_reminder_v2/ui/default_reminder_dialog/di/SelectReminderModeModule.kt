package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.di

import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeContract
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeDialog
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeDialog.ArgReminderMode
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeDialog.Companion.ARG_REMINDER_MODE
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.ui.default_reminder_dialog.SelectReminderModeViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SelectReminderModeModule {

    companion object {

        @Provides
        fun initialState(fragment: SelectReminderModeDialog): SelectReminderModeContract.State {
            val reminderMode = fragment.arguments?.getSerializable(ARG_REMINDER_MODE) as? ArgReminderMode
            return SelectReminderModeContract.State(
                customerId = reminderMode?.customerId,
                reminderMode = reminderMode?.reminderMode
            )
        }

        @Provides
        fun viewModel(
            fragment: SelectReminderModeDialog,
            viewModelProvider: Provider<SelectReminderModeViewModel>,
        ): MviViewModel<SelectReminderModeContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
