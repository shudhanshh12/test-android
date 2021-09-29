package `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class CustomerTxnAlertDialogModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: CustomerTxnAlertDialogScreen): CustomerTxnAlertDialogContract.Navigator

    companion object {

        @Provides
        fun initialState(): CustomerTxnAlertDialogContract.State = CustomerTxnAlertDialogContract.State()

        @Provides
        @ViewModelParam("customer_id")
        fun paymentMode(addNumberDialogScreen: CustomerTxnAlertDialogScreen): String {
            return addNumberDialogScreen.arguments?.getString("customer_id")
                ?: throw Exception("Customer Id shouldn't be null")
        }

        @Provides
        @ViewModelParam("description")
        fun description(addNumberDialogScreen: CustomerTxnAlertDialogScreen): String {
            return addNumberDialogScreen.arguments?.getString("description")
                ?: throw Exception("Description shouldn't be null")
        }

        @Provides
        @ViewModelParam("profilePic")
        fun profilePic(addNumberDialogScreen: CustomerTxnAlertDialogScreen): String? {
            return addNumberDialogScreen.arguments?.getString("profilePic")
        }

        @Provides
        @ViewModelParam("mobile")
        fun mobile(addNumberDialogScreen: CustomerTxnAlertDialogScreen): String? {
            return addNumberDialogScreen.arguments?.getString("mobile")
        }

        @Provides
        fun viewModel(
            fragment: CustomerTxnAlertDialogScreen,
            viewModelProvider: Provider<CustomerTxnAlertDialogViewModel>
        ): MviViewModel<CustomerTxnAlertDialogContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
