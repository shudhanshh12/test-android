package `in`.okcredit.sales_ui.ui.add_sales._di

import `in`.okcredit.sales_ui.ui.add_sales.AddSaleContract
import `in`.okcredit.sales_ui.ui.add_sales.AddSaleFragment
import `in`.okcredit.sales_ui.ui.add_sales.AddSaleViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class AddSaleModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: AddSaleFragment): AddSaleContract.Navigator

    companion object {
        @Provides
        fun initialState(): AddSaleContract.State = AddSaleContract.State()

        @Provides
        fun viewModel(
            fragment: AddSaleFragment,
            viewModelProvider: Provider<AddSaleViewModel>
        ): MviViewModel<AddSaleContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
