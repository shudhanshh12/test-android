package `in`.okcredit.user_migration.presentation.ui.upload_status.di

import `in`.okcredit.shared.base.MviViewModel
import `in`.okcredit.user_migration.presentation.ui.upload_status.screen.UploadFileStatusContract
import `in`.okcredit.user_migration.presentation.ui.upload_status.screen.UploadFileStatusFragment
import `in`.okcredit.user_migration.presentation.ui.upload_status.screen.UploadFileStatusFragmentArgs
import `in`.okcredit.user_migration.presentation.ui.upload_status.screen.UploadFileStatusViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class UploadFileStatusModule {

    companion object {
        @Provides
        fun initialState(): UploadFileStatusContract.State = UploadFileStatusContract.State()

        @Provides
        fun arguments(fragment: UploadFileStatusFragment) =
            UploadFileStatusFragmentArgs.fromBundle(fragment.requireArguments())

        @Provides
        fun viewModel(
            fragment: UploadFileStatusFragment,
            viewModelProvider: Provider<UploadFileStatusViewModel>
        ): MviViewModel<UploadFileStatusContract.State> =
            fragment.createViewModel(viewModelProvider)
    }
}
