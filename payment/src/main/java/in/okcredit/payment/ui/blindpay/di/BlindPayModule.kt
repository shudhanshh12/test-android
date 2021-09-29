package `in`.okcredit.payment.ui.blindpay.di

import `in`.okcredit.payment.R
import `in`.okcredit.payment.ui.blindpay.BlindPayContract
import `in`.okcredit.payment.ui.blindpay.BlindPayDialog
import `in`.okcredit.payment.ui.blindpay.BlindPayDialog.Companion.ARG_ACCOUNT_ID
import `in`.okcredit.payment.ui.blindpay.BlindPayDialog.Companion.ARG_LEDGER_TYPE
import `in`.okcredit.payment.ui.blindpay.BlindPayViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider

@Module
class BlindPayModule {

    @Provides
    fun initialState(fragment: BlindPayDialog): BlindPayContract.State {
        val ledgerType = fragment.requireArguments().getString(ARG_LEDGER_TYPE, "")
        val accountId = fragment.requireArguments().getString(ARG_ACCOUNT_ID, "")
        return BlindPayContract.State().copy(
            ledgerType = ledgerType,
            accountId = accountId,
            supportMsg = fragment.getString(R.string.t_002_i_need_help_generic)
        )
    }

    companion object {
        @Provides
        fun viewModel(
            fragment: BlindPayDialog,
            viewModelProvider: Provider<BlindPayViewModel>,
        ): MviViewModel<BlindPayContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
