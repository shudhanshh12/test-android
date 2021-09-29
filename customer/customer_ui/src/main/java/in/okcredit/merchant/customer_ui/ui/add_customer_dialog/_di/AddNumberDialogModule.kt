package `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog._di

import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogContract
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddNumberDialogModule {

    companion object {

        @Provides
        fun initialState(): AddNumberDialogContract.State = AddNumberDialogContract.State()

        @Provides
        @ViewModelParam("customer_id")
        fun paymentMode(addNumberDialogScreen: AddNumberDialogScreen): String {
            return addNumberDialogScreen.arguments?.getString("customer_id")
                ?: throw Exception("Customer Id shouldn't be null")
        }

        @Provides
        @ViewModelParam("description")
        fun description(addNumberDialogScreen: AddNumberDialogScreen): String {
            return addNumberDialogScreen.arguments?.getString("description")
                ?: throw Exception("Description shouldn't be null")
        }

        @Provides
        @ViewModelParam("mobile")
        fun mobile(addNumberDialogScreen: AddNumberDialogScreen): String? {
            return addNumberDialogScreen.arguments?.getString("mobile")
        }

        @Provides
        @ViewModelParam("is_skip_and_send")
        fun skipAndSend(addNumberDialogScreen: AddNumberDialogScreen): Boolean {
            return addNumberDialogScreen.arguments?.getBoolean("is_skip_and_send") ?: false
        }

        @Provides
        @ViewModelParam("is_supplier")
        fun isSupplier(addNumberDialogScreen: AddNumberDialogScreen): Boolean {
            return addNumberDialogScreen.arguments?.getBoolean("is_supplier") ?: false
        }

        @Provides
        @ViewModelParam("screen")
        fun screen(addNumberDialogScreen: AddNumberDialogScreen): String? {
            return addNumberDialogScreen.arguments?.getString("screen")
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: AddNumberDialogScreen,
            viewModelProvider: Provider<AddNumberDialogViewModel>
        ): MviViewModel<AddNumberDialogContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
