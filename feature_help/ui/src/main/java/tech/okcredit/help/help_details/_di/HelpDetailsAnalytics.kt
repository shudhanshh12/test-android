package tech.okcredit.help.help_details._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.help.help_details.HelpDetailsContract
import tech.okcredit.help.help_details.HelpDetailsFragment
import tech.okcredit.help.help_details.HelpDetailsFragmentArgs
import tech.okcredit.help.help_details.HelpDetailsViewModel
import javax.inject.Provider

@Module
abstract class HelpDetailsModule {

    companion object {

        @Provides
        @ViewModelParam(HelpDetailsViewModel.HELP_ITEM_ID)
        fun helpItemId(fragment: HelpDetailsFragment): String {
            return HelpDetailsFragmentArgs.fromBundle(fragment.requireArguments()).helpItemId ?: ""
        }

        @Provides
        @ViewModelParam(HelpDetailsViewModel.ARG_SOURCE)
        fun source(helpDetailsFragment: HelpDetailsFragment): String {
            return HelpDetailsFragmentArgs.fromBundle(helpDetailsFragment.requireArguments()).source ?: ""
        }

        @Provides
        fun initialState(): HelpDetailsContract.State = HelpDetailsContract.State()

        @Provides
        fun viewModel(
            fragment: HelpDetailsFragment,
            viewModelProvider: Provider<HelpDetailsViewModel>
        ): MviViewModel<HelpDetailsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
