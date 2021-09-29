package `in`.okcredit.sales_ui.ui.add_bill_items._di

import `in`.okcredit.sales_sdk.models.BillModel
import `in`.okcredit.sales_ui.ui.add_bill_items.AddBillItemsContract
import `in`.okcredit.sales_ui.ui.add_bill_items.AddBillItemsFragment
import `in`.okcredit.sales_ui.ui.add_bill_items.AddBillItemsViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddBillItemModule {
    companion object {

        @Provides
        fun initialState(): AddBillItemsContract.State = AddBillItemsContract.State()

        @Provides
        @ViewModelParam("bill_items")
        fun billItems(fragment: AddBillItemsFragment): BillModel.BillItems? {
            return fragment.arguments?.getParcelable("bill_items") as? BillModel.BillItems
        }

        @Provides
        fun viewModel(
            fragment: AddBillItemsFragment,
            viewModelProvider: Provider<AddBillItemsViewModel>
        ): MviViewModel<AddBillItemsContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
