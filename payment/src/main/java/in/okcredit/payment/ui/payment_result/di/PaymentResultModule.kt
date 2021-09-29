package `in`.okcredit.payment.ui.payment_result.di

import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_BLIND_PAY_FLOW
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_ACCOUNT_ID
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_DESTINATION_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_MOBILE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_NAME
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_PAYMENT_ADDRESS
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_RISK_TYPE
import `in`.okcredit.payment.ui.payment_result.PaymentResultContract
import `in`.okcredit.payment.ui.payment_result.PaymentResultFragment
import `in`.okcredit.payment.ui.payment_result.PaymentResultViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class PaymentResultModule {

    companion object {

        @Provides
        @ViewModelParam(ARG_PAYMENT_ACCOUNT_ID)
        fun supplierIdPaymentResult(fragment: PaymentResultFragment): String {
            return fragment.requireArguments().getString(ARG_PAYMENT_ACCOUNT_ID, "")
        }

        @Provides
        @ViewModelParam(PaymentResultFragment.ARG_PAYMENT_ID_PAYMENT_RESULT)
        fun paymentIdPaymentResult(fragment: PaymentResultFragment): String {
            return fragment.requireArguments().getString(PaymentResultFragment.ARG_PAYMENT_ID_PAYMENT_RESULT, "")
        }

        @Provides
        @ViewModelParam(PaymentResultFragment.ARG_PAYMENT_TYPE_RESULT)
        fun typePaymentResult(fragment: PaymentResultFragment): String {
            return fragment.requireArguments().getString(PaymentResultFragment.ARG_PAYMENT_TYPE_RESULT, "")
        }

        @Provides
        @ViewModelParam(PaymentResultFragment.ARG_PAYMENT_SHOW_TXN_CANCELLED)
        fun showTxnCancelled(fragment: PaymentResultFragment): Boolean {
            return fragment.requireArguments().getBoolean(PaymentResultFragment.ARG_PAYMENT_SHOW_TXN_CANCELLED, false)
        }

        @Provides
        @ViewModelParam(ARG_PAYMENT_MOBILE)
        fun mobile(fragment: PaymentResultFragment): String {
            return fragment.requireArguments().getString(ARG_PAYMENT_MOBILE, "")
        }

        @Provides
        @ViewModelParam(ARG_ACCOUNT_TYPE)
        fun accountType(fragment: PaymentResultFragment): String {
            return fragment.requireArguments().getString(ARG_ACCOUNT_TYPE, "")
        }

        @Provides
        fun initialState(fragment: PaymentResultFragment): PaymentResultContract.State {

            val paymentAddress = fragment.requireArguments().getString(ARG_PAYMENT_PAYMENT_ADDRESS, "")
            val destinationType = fragment.requireArguments().getString(ARG_PAYMENT_DESTINATION_TYPE, "")
            val name = fragment.requireArguments().getString(ARG_PAYMENT_NAME, "")
            val mobile = fragment.requireArguments().getString(ARG_PAYMENT_MOBILE, "")
            val accountType = fragment.requireArguments().getString(ARG_ACCOUNT_TYPE, "")
            val blindPayFlow = fragment.requireArguments().getBoolean(ARG_BLIND_PAY_FLOW, false)
            return PaymentResultContract.State().copy(
                riskType = fragment.requireArguments().getString(ARG_PAYMENT_RISK_TYPE, "LOW"),
                accountId = fragment.requireArguments()
                    .getString(ARG_PAYMENT_ACCOUNT_ID, ""),
                paymentAddress = paymentAddress,
                destinationType = destinationType,
                name = name,
                mobile = mobile,
                accountType = accountType,
                blindPayFlow = blindPayFlow,
            )
        }

        @Provides
        fun viewModel(
            fragment: PaymentResultFragment,
            viewModelProvider: Provider<PaymentResultViewModel>,
        ): MviViewModel<PaymentResultContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
