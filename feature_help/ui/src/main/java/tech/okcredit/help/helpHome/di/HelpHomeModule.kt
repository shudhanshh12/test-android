package tech.okcredit.help.helpHome.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.help.helpHome.HelpHomeContract
import tech.okcredit.help.helpHome.HelpHomeFragment
import tech.okcredit.help.helpHome.HelpHomeViewModel
import javax.inject.Provider

@Module
class HelpHomeModule {
    companion object {

        @Provides
        @ViewModelParam(HelpHomeViewModel.HELP_ID)
        fun helpId(helpHomeFragment: HelpHomeFragment): List<String> {
            return helpHomeFragment.activity?.intent?.getStringArrayListExtra(HelpHomeViewModel.HELP_ID) ?: arrayListOf()
        }

        @Provides
        @ViewModelParam(HelpHomeViewModel.ARG_SOURCE)
        fun source(helpHomeFragment: HelpHomeFragment): String {
            return helpHomeFragment.activity?.intent?.getStringExtra(HelpHomeViewModel.ARG_SOURCE) ?: ""
        }

        @Provides
        fun initialState(): HelpHomeContract.State = HelpHomeContract.State()

        @Provides
        fun viewModel(
            fragment: HelpHomeFragment,
            viewModelProvider: Provider<HelpHomeViewModel>
        ): MviViewModel<HelpHomeContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
