package tech.okcredit.home.dialogs.supplier_payment_dialog._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.dialogs.supplier_payment_dialog.SupplierPaymentContract
import tech.okcredit.home.dialogs.supplier_payment_dialog.SupplierPaymentDialog
import tech.okcredit.home.dialogs.supplier_payment_dialog.SupplierPaymentViewModel
import javax.inject.Provider

@Module
abstract class SupplierPaymentModule {

    companion object {

        @Provides
        fun initialState(): SupplierPaymentContract.State = SupplierPaymentContract.State()

        @Provides
        @ViewModelParam(SupplierPaymentContract.ARG_SUPPLIER_ID)
        fun supplierId(addPaymentDestinationDialog: SupplierPaymentDialog): String {
            return addPaymentDestinationDialog.arguments?.getString(SupplierPaymentContract.ARG_SUPPLIER_ID) ?: ""
        }

        // @FragmentScope not required here because Presenter lifecycle is managed by android view model system (no need for the fragment sub component to handle it)
        @Provides
        fun viewModel(
            fragment: SupplierPaymentDialog,
            viewModelProvider: Provider<SupplierPaymentViewModel>
        ): MviViewModel<SupplierPaymentContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
