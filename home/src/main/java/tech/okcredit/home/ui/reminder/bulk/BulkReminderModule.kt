package tech.okcredit.home.ui.reminder.bulk

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class BulkReminderModule {

    companion object {
        @Provides
        fun initialState(): BulkReminderContract.State = BulkReminderContract.State()

        @Provides
        fun viewModel(
            fragment: BulkReminderBottomSheet,
            viewModelProvider: Provider<BulkReminderViewModel>
        ): MviViewModel<BulkReminderContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
