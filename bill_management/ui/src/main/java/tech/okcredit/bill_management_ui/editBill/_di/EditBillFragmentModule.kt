package tech.okcredit.bill_management_ui.editBill._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.editBill.EditBillContract
import tech.okcredit.bill_management_ui.editBill.EditBillFragment
import tech.okcredit.bill_management_ui.editBill.EditBillViewModel
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import javax.inject.Provider

@Module
abstract class EditBillFragmentModule {

    companion object {

        @Provides
        fun initialState(): EditBillContract.State = EditBillContract.State()

        @Provides
        fun viewModel(
            fragment: EditBillFragment,
            viewModelProviderEdit: Provider<EditBillViewModel>
        ): MviViewModel<EditBillContract.State> = fragment.createViewModel(viewModelProviderEdit)

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.BILL_ID)
        fun getBillId(editBillFragment: EditBillFragment): String? {
            return editBillFragment.activity?.intent?.getStringExtra(BILL_INTENT_EXTRAS.BILL_ID)
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.BILL_POSITION)
        fun getBillPosition(editBillFragment: EditBillFragment): Int? {
            return editBillFragment.activity?.intent?.getIntExtra(BILL_INTENT_EXTRAS.BILL_POSITION, 0)
        }
    }
}
