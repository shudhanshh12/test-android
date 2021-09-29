package tech.okcredit.home.ui._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.home.ui.menu.HomeMenuContract.*
import tech.okcredit.home.ui.menu.HomeMenuFragment
import tech.okcredit.home.ui.menu.HomeMenuViewModel
import javax.inject.Provider

@Module
abstract class HomeMenuFragmentModule {

    companion object {

        @Provides
        fun initialState(): HomeMenuState = HomeMenuState()

        @Provides
        fun viewModel(
            fragmentHome: HomeMenuFragment,
            viewModelProviderHome: Provider<HomeMenuViewModel>
        ): MviViewModel<HomeMenuState> = fragmentHome.createViewModel(viewModelProviderHome)
    }
}
