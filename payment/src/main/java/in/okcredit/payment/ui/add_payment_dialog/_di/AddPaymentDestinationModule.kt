package `in`.okcredit.payment.ui.add_payment_dialog._di

import `in`.okcredit.payment.PaymentActivity
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.payment.R
import `in`.okcredit.payment.ui.add_payment_dialog.AddPaymentDestinationContract
import `in`.okcredit.payment.ui.add_payment_dialog.AddPaymentDestinationDialog
import `in`.okcredit.payment.ui.add_payment_dialog.AddPaymentDestinationViewModel
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorFragment.Companion.ARG_ACCOUNT_ID
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class AddPaymentDestinationModule {

    companion object {

        @Provides
        fun initialState(fragment: AddPaymentDestinationDialog): AddPaymentDestinationContract.State {
            val accountBalance = fragment.requireArguments().getLong(PaymentActivity.ARG_ACCOUNT_BALANCE, 0L)
            val mobile = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_MOBILE, "")
            val name = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_NAME, "")
            val accountId = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_ACCOUNT_ID, "")
            val profileImage = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_PROFILE_IMAGE, "")
            val accountType = fragment.requireArguments().getString(PaymentActivity.ARG_ACCOUNT_TYPE, "")
            return AddPaymentDestinationContract.State().copy(
                accountId = accountId,
                mobile = mobile,
                dueBalance = accountBalance,
                name = name,
                profileImage = profileImage,
                accountType = accountType,
                supportMsg = fragment.getString(R.string.t_002_i_need_help_generic)
            )
        }

        @Provides
        @ViewModelParam(ARG_ACCOUNT_ID)
        fun accountId(addPaymentDestinationDialog: AddPaymentDestinationDialog): String {
            return addPaymentDestinationDialog.arguments?.getString(ARG_ACCOUNT_ID) ?: ""
        }

        @Provides
        @ViewModelParam(ARG_ACCOUNT_TYPE)
        fun accountType(addPaymentDestinationDialog: AddPaymentDestinationDialog): String {
            return addPaymentDestinationDialog.arguments?.getString(ARG_ACCOUNT_TYPE) ?: ""
        }

        @Provides
        fun viewModel(
            fragment: AddPaymentDestinationDialog,
            viewModelProvider: Provider<AddPaymentDestinationViewModel>,
        ): MviViewModel<AddPaymentDestinationContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
