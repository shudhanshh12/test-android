package merchant.okcredit.accounting.ui.customer_support_option_dialog.di

import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import merchant.okcredit.accounting.R
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionContract
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog.Companion.ARG_ACCOUNT_ID
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog.Companion.ARG_AMOUNT
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog.Companion.ARG_LEDGER_TYPE
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog.Companion.ARG_PAYMENT_ID
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog.Companion.ARG_PAYMENT_TIME
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog.Companion.ARG_SOURCE
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog.Companion.ARG_STATUS
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionViewModel
import merchant.okcredit.accounting.utils.AccountingSharedUtils.getWhatsAppMsg
import tech.okcredit.android.base.TempCurrencyUtil
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
class CustomerSupportOptionModule {

    @Provides
    fun initialState(fragment: CustomerSupportOptionDialog): CustomerSupportOptionContract.State {
        val amount = fragment.requireArguments().getString(ARG_AMOUNT, "")
        val paymentTime = fragment.requireArguments().getString(ARG_PAYMENT_TIME, "")
        val txnId = fragment.requireArguments().getString(ARG_PAYMENT_ID, "")
        val status = fragment.requireArguments().getString(ARG_STATUS, "")
        val accountId = fragment.requireArguments().getString(ARG_ACCOUNT_ID, "")
        val ledgerType = fragment.requireArguments().getString(ARG_LEDGER_TYPE, "")
        val source = fragment.requireArguments().getString(ARG_SOURCE, "")

        return CustomerSupportOptionContract.State().copy(
            supportMsg =
            getWhatsAppMsg(
                fragment.requireContext(),
                fragment.getString(R.string.earned_amount, TempCurrencyUtil.formatV2(amount.toLong())),
                paymentTime,
                txnId,
                status
            ),
            accountId = accountId,
            amount = amount,
            txnId = txnId,
            ledgerType = ledgerType,
            source = source,
        )
    }

    companion object {
        @Provides
        fun viewModel(
            fragment: CustomerSupportOptionDialog,
            viewModelProvider: Provider<CustomerSupportOptionViewModel>,
        ): MviViewModel<CustomerSupportOptionContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
