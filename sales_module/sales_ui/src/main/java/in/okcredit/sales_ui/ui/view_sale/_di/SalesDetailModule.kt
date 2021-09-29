package `in`.okcredit.sales_ui.ui.view_sale._di

import `in`.okcredit.sales_ui.ui.view_sale.SalesDetailContract
import `in`.okcredit.sales_ui.ui.view_sale.SalesDetailFragment
import `in`.okcredit.sales_ui.ui.view_sale.SalesDetailViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class SalesDetailModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: SalesDetailFragment): SalesDetailContract.Navigator

    companion object {

        @Provides
        fun initialState(): SalesDetailContract.State = SalesDetailContract.State()

        @Provides
        @ViewModelParam("sale_id")
        fun saleId(fragment: SalesDetailFragment): String {
            return fragment.arguments?.getString("sale_id") ?: ""
        }

        @Provides
        fun viewModel(
            fragment: SalesDetailFragment,
            viewModelProvider: Provider<SalesDetailViewModel>
        ): MviViewModel<SalesDetailContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
