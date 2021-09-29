package `in`.okcredit.frontend.ui.add_expense._di

import `in`.okcredit.frontend.ui.add_expense.AddExpenseContract
import `in`.okcredit.frontend.ui.add_expense.AddExpenseFragment
import `in`.okcredit.frontend.ui.add_expense.AddExpenseViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
abstract class AddExpenseModule {

    companion object {

        @Provides
        fun initialState(): AddExpenseContract.State = AddExpenseContract.State()

        @Provides
        fun viewModel(
            fragment: AddExpenseFragment,
            viewModelProvider: Provider<AddExpenseViewModel>
        ): MviViewModel<AddExpenseContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
