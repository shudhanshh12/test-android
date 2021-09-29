package tech.okcredit.home.ui._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.ui.homesearch.HomeSearchContract
import tech.okcredit.home.ui.homesearch.HomeSearchFragment
import tech.okcredit.home.ui.homesearch.HomeSearchViewModel
import javax.inject.Provider

@Module
abstract class HomeSearchModule {

    companion object {

        @Provides
        @ViewModelParam(HomeSearchFragment.ARG_SOURCE)
        fun source(fragment: HomeSearchFragment): String {
            return fragment.arguments?.getString(HomeSearchFragment.ARG_SOURCE)
                ?: HomeSearchContract.SOURCE.HOME_CUSTOMER_TAB.value
        }

        @Provides
        @ViewModelParam(HomeSearchFragment.ACCOUNT_SELECTION)
        fun customerSelection(fragment: HomeSearchFragment): Boolean {
            return fragment.arguments?.getBoolean(HomeSearchFragment.ACCOUNT_SELECTION) ?: false
        }

        @Provides
        fun initialState(): HomeSearchContract.State = HomeSearchContract.State()

        @Provides
        fun viewModel(
            fragment: HomeSearchFragment,
            viewModelProvider: Provider<HomeSearchViewModel>
        ): MviViewModel<HomeSearchContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
