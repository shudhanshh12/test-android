package `in`.okcredit.payment.ui.payment_destination._di

import `in`.okcredit.payment.ui.payment_destination.PaymentDestinationContract
import `in`.okcredit.payment.ui.payment_destination.PaymentDestinationDialog
import `in`.okcredit.payment.ui.payment_destination.PaymentDestinationViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class PaymentDestinationModule {

    companion object {

        @Provides
        fun initialState(dialog: PaymentDestinationDialog): PaymentDestinationContract.State {
            val showUpiOption = dialog.arguments?.getBoolean(PaymentDestinationDialog.ARG_SHOW_UPI) ?: false
            val descText = dialog.arguments?.getBoolean(PaymentDestinationDialog.ARG_DESC_TEXT) ?: false
            return PaymentDestinationContract.State().copy(showUpiOption = showUpiOption, showDescText = descText)
        }

        @Provides
        @ViewModelParam(PaymentDestinationDialog.ARG_SOURCE)
        fun sourceFrom(dialog: PaymentDestinationDialog): String {
            return dialog.arguments?.getString(PaymentDestinationDialog.ARG_SOURCE) ?: ""
        }

        @Provides
        fun viewModel(
            fragment: PaymentDestinationDialog,
            viewModelProvider: Provider<PaymentDestinationViewModel>
        ): MviViewModel<PaymentDestinationContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
