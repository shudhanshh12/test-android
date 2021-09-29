package tech.okcredit.home.dialogs.customer_profile_dialog._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.base.dagger.di.scope.FragmentScope
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialog
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialog.Companion.CUSTOMER_ID
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialogContract
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialogViewModel
import javax.inject.Provider

@Module
abstract class CustomerProfileDialogModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: CustomerProfileDialog): CustomerProfileDialogContract.Navigator

    companion object {

        @Provides
        fun initialState(): CustomerProfileDialogContract.State = CustomerProfileDialogContract.State()

        @Provides
        @ViewModelParam(CustomerProfileDialogContract.ARG_CUSTOMER_ID)
        fun supplierId(customerProfileDialog: CustomerProfileDialog): String {
            return customerProfileDialog.arguments?.getString(CUSTOMER_ID) ?: ""
        }

        @Provides
        fun viewModel(
            fragment: CustomerProfileDialog,
            viewModelProvider: Provider<CustomerProfileDialogViewModel>,
        ): MviViewModel<CustomerProfileDialogContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
