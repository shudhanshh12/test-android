package `in`.okcredit.user_migration.presentation.ui.file_pick.di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.FilePickContract
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.FilePickViewModel
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.FilePickerFragment
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.views.ItemViewFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class UploadFileListModule {

    @Binds
    abstract fun eventListener(fragment: FilePickerFragment): ItemViewFile.ItemViewFileListener

    companion object {
        @Provides
        fun initialState(): FilePickContract.State = FilePickContract.State()

        @Provides
        fun viewModel(
            fragment: FilePickerFragment,
            pickviewModelProvider: Provider<FilePickViewModel>
        ): MviViewModel<FilePickContract.State> = fragment.createViewModel(pickviewModelProvider)
    }
}
