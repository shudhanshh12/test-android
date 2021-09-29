package merchant.okcredit.accounting.ui.customer_support_exit_dialog.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import merchant.okcredit.accounting.R
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitContract
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitDialog
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitDialog.Companion.ARG_ACCOUNT_ID
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitDialog.Companion.ARG_LEDGER_TYPE
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitDialog.Companion.ARG_SOURCE
import merchant.okcredit.accounting.ui.customer_support_exit_dialog.CustomerSupportExitViewModel
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
class CustomerSupportExitModule {

    @Provides
    fun initialState(fragment: CustomerSupportExitDialog): CustomerSupportExitContract.State {
        val helpString = fragment.getString(R.string.t_002_i_need_help_generic)
        return CustomerSupportExitContract.State().copy(
            ledgerType = fragment.requireArguments().getString(ARG_LEDGER_TYPE, ""),
            supportMsg = fragment.getString(R.string.whatsapp_mono_space_template, helpString),
            accountId = fragment.requireArguments().getString(ARG_ACCOUNT_ID, ""),
            source = fragment.requireArguments().getString(ARG_SOURCE, ""),
        )
    }

    companion object {
        @Provides
        fun viewModel(
            fragment: CustomerSupportExitDialog,
            viewModelProvider: Provider<CustomerSupportExitViewModel>,
        ): MviViewModel<CustomerSupportExitContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
