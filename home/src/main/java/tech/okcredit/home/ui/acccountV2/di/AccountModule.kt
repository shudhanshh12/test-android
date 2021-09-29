package tech.okcredit.home.ui.acccountV2.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.ui.acccountV2.ui.AccountContract
import tech.okcredit.home.ui.acccountV2.ui.AccountFragment
import tech.okcredit.home.ui.acccountV2.ui.AccountViewModel
import javax.inject.Provider

@Module
class AccountModule {
    companion object {

        @Provides
        fun initialState(): AccountContract.State = AccountContract.State()

        @Provides
        @ViewModelParam("notification_url")
        fun notificationUrl(fragment: AccountFragment): String {
            return fragment.arguments?.getString("notification_url") ?: ""
        }

        @Provides
        fun viewModel(
            fragment: AccountFragment,
            viewModelProvider: Provider<AccountViewModel>
        ): MviViewModel<AccountContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
