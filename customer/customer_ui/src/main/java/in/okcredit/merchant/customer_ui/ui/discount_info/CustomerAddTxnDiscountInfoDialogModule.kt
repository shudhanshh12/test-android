package `in`.okcredit.merchant.customer_ui.ui.discount_info

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class CustomerAddTxnDiscountInfoDialogModule {

    companion object {

        @Provides
        fun initialState(): CustomerAddTxnDiscountInfoDialogContract.State = CustomerAddTxnDiscountInfoDialogContract.State()

        @Provides
        @ViewModelParam("amount")
        fun paymentMode(customerAddTxnDiscountInfoDialogScreen: CustomerAddTxnDiscountInfoDialogScreen): String {
            return customerAddTxnDiscountInfoDialogScreen.arguments?.getString("amount")
                ?: throw Exception("amount shouldn't be null")
        }

        @Provides
        @ViewModelParam("discounted_amount")
        fun description(customerAddTxnDiscountInfoDialogScreen: CustomerAddTxnDiscountInfoDialogScreen): String {
            return customerAddTxnDiscountInfoDialogScreen.arguments?.getString("discounted_amount")
                ?: throw Exception("discounted_amount shouldn't be null")
        }

        @Provides
        fun viewModel(
            fragment: CustomerAddTxnDiscountInfoDialogScreen,
            viewModelProvider: Provider<CustomerAddTxnDiscountInfoDialogViewModel>
        ): MviViewModel<CustomerAddTxnDiscountInfoDialogContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
