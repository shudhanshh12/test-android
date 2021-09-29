package `in`.okcredit.collection_ui.ui.home_menu

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class HomePaymentsContainerModule {

    companion object {

        @Provides
        fun initialState(): HomePaymentsContainerContract.State = HomePaymentsContainerContract.State()

        @Provides
        fun viewModel(
            fragment: HomePaymentsContainerFragment,
            viewModelProvider: Provider<HomePaymentsContainerViewModel>,
        ): MviViewModel<HomePaymentsContainerContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
