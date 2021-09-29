package tech.okcredit.help.helpcontactus._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.app_contract.AppConstants
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.help.helpcontactus.HelpContactUsContract
import tech.okcredit.help.helpcontactus.HelpContactUsFragment
import tech.okcredit.help.helpcontactus.HelpContactUsViewModel
import javax.inject.Provider

@Module
abstract class HelpContactUsModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: HelpContactUsFragment): HelpContactUsContract.Navigator

    companion object {

        @Provides
        fun initialState(): HelpContactUsContract.State = HelpContactUsContract.State()

        @Provides
        @ViewModelParam(HelpContactUsViewModel.HELP_ID)
        fun helpContactUs(helpContactUsFragment: HelpContactUsFragment): List<String> {
            return helpContactUsFragment.activity?.intent?.getStringArrayListExtra(HelpContactUsViewModel.HELP_ID)
                ?: arrayListOf()
        }

        @Provides
        @ViewModelParam(AppConstants.ARG_SOURCE)
        fun source(helpContactUsFragment: HelpContactUsFragment): String {
            return helpContactUsFragment.activity?.intent?.getStringExtra(AppConstants.ARG_SOURCE) ?: ""
        }

        @Provides
        fun viewModel(
            fragment: HelpContactUsFragment,
            viewModelProvider: Provider<HelpContactUsViewModel>
        ): MviViewModel<HelpContactUsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
