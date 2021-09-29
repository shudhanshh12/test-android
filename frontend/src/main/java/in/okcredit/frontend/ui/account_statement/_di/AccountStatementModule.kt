package `in`.okcredit.frontend.ui.account_statement._di

import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract.*
import `in`.okcredit.frontend.ui.account_statement.AccountStatementFragment
import `in`.okcredit.frontend.ui.account_statement.AccountStatementViewModel
import `in`.okcredit.frontend.ui.account_statement.AccountStatementViewModel.Companion.ARG_DURATION
import `in`.okcredit.frontend.ui.account_statement.AccountStatementViewModel.Companion.ARG_FILTER
import `in`.okcredit.shared.base.MviViewModel
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AccountStatementModule {

    companion object {
        @Provides
        fun initialState(): State = State()

        @Provides
        @ViewModelParam(MainActivity.ARG_SOURCE)
        fun source(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(MainActivity.ARG_SOURCE) ?: ""
        }

        @Provides
        @ViewModelParam(ARG_DURATION)
        fun duration(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(ARG_DURATION) ?: ""
        }

        @Provides
        @ViewModelParam(ARG_FILTER)
        fun filter(activity: AppCompatActivity): String {
            return activity.intent.getStringExtra(ARG_FILTER) ?: ""
        }

        @Provides
        fun viewModel(
            fragment: AccountStatementFragment,
            viewModelProvider: Provider<AccountStatementViewModel>
        ): MviViewModel<State> = fragment.createViewModel(viewModelProvider)
    }
}
