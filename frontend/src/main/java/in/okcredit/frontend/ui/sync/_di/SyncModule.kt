package `in`.okcredit.frontend.ui.sync._di

import `in`.okcredit.frontend.ui.sync.SyncContract
import `in`.okcredit.frontend.ui.sync.SyncFragment
import `in`.okcredit.frontend.ui.sync.SyncViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class SyncModule {

    companion object {

        @Provides
        fun initialState(): SyncContract.State = SyncContract.State()

        @Provides
        fun viewModel(
            fragment: SyncFragment,
            viewModelProvider: Provider<SyncViewModel>
        ): MviViewModel<SyncContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
