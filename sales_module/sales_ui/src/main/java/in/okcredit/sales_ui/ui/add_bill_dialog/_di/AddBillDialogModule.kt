package `in`.okcredit.sales_ui.ui.add_bill_dialog._di

import `in`.okcredit.sales_ui.ui.add_bill_dialog.AddBillBottomSheetDialog
import `in`.okcredit.sales_ui.ui.add_bill_dialog.AddBillContract
import `in`.okcredit.sales_ui.ui.add_bill_dialog.AddBillViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.scope.FragmentScope
import javax.inject.Provider

@Module
abstract class AddBillDialogModule {

    @Binds
    @FragmentScope
    abstract fun navigator(fragment: AddBillBottomSheetDialog): AddBillContract.Navigator

    companion object {

        @Provides
        fun initialState(): AddBillContract.State = AddBillContract.State()

        @Provides
        fun viewModel(
            fragment: AddBillBottomSheetDialog,
            viewModelProvider: Provider<AddBillViewModel>
        ): MviViewModel<AddBillContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
