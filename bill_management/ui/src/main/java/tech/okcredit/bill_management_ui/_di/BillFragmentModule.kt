package tech.okcredit.bill_management_ui._di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.BillGlobalInfo
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.bill_management_ui.BillContract
import tech.okcredit.bill_management_ui.BillFragment
import tech.okcredit.bill_management_ui.BillViewModel
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import javax.inject.Provider

@Module
abstract class BillFragmentModule {

    companion object {

        @Provides
        fun initialState(): BillContract.State {
            return BillContract.State()
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_ID)
        fun getAccountId(billFragment: BillFragment): String {
            val accountId = billFragment.activity?.intent?.getStringExtra(BILL_INTENT_EXTRAS.ACCOUNT_ID)
            BillGlobalInfo.accountId = accountId!!
            return accountId
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.ACCOUNT_NAME)
        fun getAccountName(billFragment: BillFragment): String? {
            return billFragment.activity?.intent?.getStringExtra(BILL_INTENT_EXTRAS.ACCOUNT_NAME)
        }

        @Provides
        @ViewModelParam(BILL_INTENT_EXTRAS.ROLE)
        fun getAccountRole(billFragment: BillFragment): String {
            val role = billFragment.activity?.intent?.getStringExtra(BILL_INTENT_EXTRAS.ROLE)
            BillGlobalInfo.relation = role!!
            return role
        }

        @Provides
        fun viewModel(
            fragment: BillFragment,
            viewModelProvider: Provider<BillViewModel>
        ): MviViewModel<BillContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
