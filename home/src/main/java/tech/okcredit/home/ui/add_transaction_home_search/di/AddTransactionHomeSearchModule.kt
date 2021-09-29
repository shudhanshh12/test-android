package tech.okcredit.home.ui.add_transaction_home_search.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchContract
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchFragment
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchFragment.Companion.ARG_ADD_TRANSACTION_SHORTCUT_SOURCE
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchFragment.Companion.ARG_REFERRAL_TARGETS
import tech.okcredit.home.ui.add_transaction_home_search.AddTransactionShortcutSearchViewModel
import javax.inject.Provider

@Module
abstract class AddTransactionHomeSearchModule {

    companion object {

        @Provides
        fun initialState(
            fragment: AddTransactionShortcutSearchFragment
        ): AddTransactionShortcutSearchContract.State = AddTransactionShortcutSearchContract.State(
            isComingFromReferralTargets = fragment.arguments
                ?.getString(ARG_ADD_TRANSACTION_SHORTCUT_SOURCE) == ARG_REFERRAL_TARGETS
        )

        @Provides
        fun viewModel(
            fragment: AddTransactionShortcutSearchFragment,
            viewModelProvider: Provider<AddTransactionShortcutSearchViewModel>
        ): MviViewModel<AddTransactionShortcutSearchContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
