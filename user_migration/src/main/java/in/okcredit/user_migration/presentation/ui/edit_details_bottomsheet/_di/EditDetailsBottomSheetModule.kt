package `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet._di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.EditDetailContract
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.EditDetailViewModel
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.EditDetailsBottomSheet
import `in`.okcredit.user_migration.presentation.ui.edit_details_bottomsheet.EditDetailsBottomSheetArgs
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class EditDetailsBottomSheetModule {

    companion object {

        @Provides
        fun arguments(fragment: EditDetailsBottomSheet) =
            EditDetailsBottomSheetArgs.fromBundle(fragment.requireArguments())

        @Provides
        fun initialState(): EditDetailContract.State = EditDetailContract.State()

        @Provides
        fun viewModel(
            fragment: EditDetailsBottomSheet,
            viewModelProvider: Provider<EditDetailViewModel>
        ): MviViewModel<EditDetailContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
