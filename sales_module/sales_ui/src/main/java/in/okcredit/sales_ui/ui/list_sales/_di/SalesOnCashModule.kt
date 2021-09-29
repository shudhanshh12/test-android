package `in`.okcredit.sales_ui.ui.list_sales._di

import `in`.okcredit.sales_ui.ui.list_sales.SalesOnCashContract
import `in`.okcredit.sales_ui.ui.list_sales.SalesOnCashFragment
import `in`.okcredit.sales_ui.ui.list_sales.SalesOnCashViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class SalesOnCashModule {
    @Binds
    @FragmentScope
    abstract fun navigator(fragment: SalesOnCashFragment): SalesOnCashContract.Navigator

    companion object {

        @Provides
        fun initialState(): SalesOnCashContract.State = SalesOnCashContract.State()

        @Provides
        fun viewModel(
            fragment: SalesOnCashFragment,
            viewModelProvider: Provider<SalesOnCashViewModel>
        ): MviViewModel<SalesOnCashContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
