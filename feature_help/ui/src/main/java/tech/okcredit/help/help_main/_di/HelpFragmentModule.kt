package tech.okcredit.help.help_main._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.help.HelpActivity
import tech.okcredit.help.help_main.HelpContract
import tech.okcredit.help.help_main.HelpFragment
import tech.okcredit.help.help_main.HelpViewModel
import tech.okcredit.userSupport.ContextualHelp
import javax.inject.Provider

@Module
abstract class HelpFragmentModule {

    companion object {

        @Provides
        @ViewModelParam(HelpActivity.HELP_ID)
        fun helpId(helpFragment: HelpFragment): List<String> {
            return helpFragment.activity?.intent?.getStringArrayListExtra(HelpActivity.HELP_ID) ?: listOf()
        }

        @Provides
        fun initialState(): HelpContract.State = HelpContract.State()

        @Provides
        @ViewModelParam(HelpActivity.EXTRA_SOURCE)
        fun source(helpFragment: HelpFragment): String {
            return helpFragment.activity?.intent?.getStringExtra(HelpActivity.EXTRA_SOURCE) ?: ""
        }

        @Provides
        @ViewModelParam(HelpActivity.EXTRA_CONTEXTUAL_HELP)
        fun contextualHelp(helpFragment: HelpFragment): ContextualHelp? {
            return helpFragment.activity?.intent?.getSerializableExtra(HelpActivity.EXTRA_CONTEXTUAL_HELP) as? ContextualHelp
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: HelpFragment,
            viewModelProvider: Provider<HelpViewModel>
        ): MviViewModel<HelpContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
