package `in`.okcredit.payment.ui.payment_error_screen.di

import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorContract
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorFragment
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorFragment.Companion.ARG_ACCOUNT_ID
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorFragment.Companion.ARG_PAYMENT_ERROR_TYPE
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorType
import `in`.okcredit.payment.ui.payment_error_screen.PaymentErrorViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class PaymentErrorModule {
    companion object {
        @Provides
        @ViewModelParam(ARG_PAYMENT_ERROR_TYPE)
        fun supplierErrorType(fragment: PaymentErrorFragment): String {
            return fragment.requireArguments().getString(ARG_PAYMENT_ERROR_TYPE, PaymentErrorType.OTHER.value)
        }

        @Provides
        @ViewModelParam(ARG_ACCOUNT_ID)
        fun supplierSupplierId(fragment: PaymentErrorFragment): String {
            return fragment.requireArguments().getString(ARG_ACCOUNT_ID, "")
        }

        @Provides
        @ViewModelParam(PaymentErrorFragment.ARG_ERROR_MSG)
        fun supplierSupplierMsg(fragment: PaymentErrorFragment): String {
            return fragment.requireArguments().getString(PaymentErrorFragment.ARG_ERROR_MSG, "")
        }

        @Provides
        fun initialState(fragment: PaymentErrorFragment): PaymentErrorContract.State {
            return PaymentErrorContract.State().copy(
                accountType = fragment.requireArguments().getString(ARG_ACCOUNT_TYPE, "")
            )
        }

        @Provides
        fun viewModel(
            fragment: PaymentErrorFragment,
            viewModelProvider: Provider<PaymentErrorViewModel>
        ): MviViewModel<PaymentErrorContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
