package `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.di_

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.UploadOptionBottomSheet
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.UploadOptionBottomSheetContract
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.UploadOptionBottomSheetViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class UploadOptionBottomSheetModule {

    companion object {
        @Provides
        fun viewModel(
            fragment: UploadOptionBottomSheet,
            viewModelProvider: Provider<UploadOptionBottomSheetViewModel>
        ): MviViewModel<UploadOptionBottomSheetContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
