package `in`.okcredit.collection_ui.ui.passbook.add_to_khata.di

import `in`.okcredit.collection_ui.ui.passbook.add_to_khata.AddToKhataContract
import `in`.okcredit.collection_ui.ui.passbook.add_to_khata.AddToKhataDialog
import `in`.okcredit.collection_ui.ui.passbook.add_to_khata.AddToKhataViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddToKhataModule {

    companion object {

        @Provides
        @ViewModelParam("customer_id")
        fun customerId(fragment: AddToKhataDialog): String {
            return fragment.arguments?.getString("customer_id") ?: ""
        }

        @Provides
        @ViewModelParam("payment_id")
        fun paymentId(fragment: AddToKhataDialog): String {
            return fragment.arguments?.getString("payment_id") ?: ""
        }

        @Provides
        fun initialState(fragment: AddToKhataDialog): AddToKhataContract.State {
            return AddToKhataContract.State(source = (fragment.arguments?.getString(AddToKhataDialog.ARG_SOURCE) ?: ""))
        }

        @Provides
        fun viewModel(
            fragment: AddToKhataDialog,
            viewModelProvider: Provider<AddToKhataViewModel>
        ): MviViewModel<AddToKhataContract.State> =
            fragment.createViewModel(viewModelProvider)
    }
}
