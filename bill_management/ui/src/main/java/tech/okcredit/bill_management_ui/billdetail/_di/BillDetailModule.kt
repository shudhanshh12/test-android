package tech.okcredit.bill_management_ui.billdetail._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.billdetail.BillDetailContract
import tech.okcredit.bill_management_ui.billdetail.BillDetailFragment
import tech.okcredit.bill_management_ui.billdetail.BillDetailViewModel
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import javax.inject.Provider

@Module
abstract class BillDetailModule {

    companion object {
        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_ID)
        fun getAccountId(billDetailFragment: BillDetailFragment): String? {
            return billDetailFragment.activity?.intent?.getStringExtra(BILL_INTENT_EXTRAS.ACCOUNT_ID)
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.BILL_ID)
        fun getBillId(billDetailFragment: BillDetailFragment): String? {
            return billDetailFragment.arguments?.getString(BILL_INTENT_EXTRAS.BILL_ID)
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.ROLE)
        fun getRole(billDetailFragment: BillDetailFragment): String? {
            return billDetailFragment.activity?.intent?.getStringExtra(BILL_INTENT_EXTRAS.ROLE)
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_NAME)
        fun getAccountName(billFragment: BillDetailFragment): String? {
            return billFragment.activity?.intent?.getStringExtra(BILL_INTENT_EXTRAS.ACCOUNT_NAME)
        }

        @Provides
        fun initialState(): BillDetailContract.State = BillDetailContract.State()

        @Provides
        fun viewModel(
            fragment: BillDetailFragment,
            viewModelProvider: Provider<BillDetailViewModel>
        ): MviViewModel<BillDetailContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
