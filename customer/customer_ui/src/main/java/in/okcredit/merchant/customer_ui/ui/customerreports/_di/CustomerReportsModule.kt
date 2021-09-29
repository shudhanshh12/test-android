package `in`.okcredit.merchant.customer_ui.ui.customerreports._di

import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsFragment
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class CustomerReportsModule {

    companion object {

        @Provides
        fun initialState(): CustomerReportsContract.State = CustomerReportsContract.State()

        @Provides
        @ViewModelParam("customer_id")
        fun customerId(fragment: CustomerReportsFragment): String {
            return fragment.arguments?.getString("customer_id") ?: ""
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system
        // (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: CustomerReportsFragment,
            viewModelProvider: Provider<CustomerReportsViewModel>
        ): MviViewModel<CustomerReportsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
