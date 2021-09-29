package `in`.okcredit.frontend.ui.expense_manager._di

import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerContract
import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerFragment
import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class ExpenseManagerModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: ExpenseManagerFragment): ExpenseManagerContract.Navigator

    companion object {

        @Provides
        fun initialState(): ExpenseManagerContract.State = ExpenseManagerContract.State()

        @Provides
        fun viewModel(
            fragment: ExpenseManagerFragment,
            viewModelProvider: Provider<ExpenseManagerViewModel>
        ): MviViewModel<ExpenseManagerContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
