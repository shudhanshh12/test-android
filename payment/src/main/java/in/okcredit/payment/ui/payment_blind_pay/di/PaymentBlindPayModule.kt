package `in`.okcredit.payment.ui.payment_blind_pay.di

import `in`.okcredit.payment.PaymentActivity
import `in`.okcredit.payment.ui.payment_blind_pay.PaymentBlindPayContract
import `in`.okcredit.payment.ui.payment_blind_pay.PaymentBlindPayFragment
import `in`.okcredit.payment.ui.payment_blind_pay.PaymentBlindPayViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import merchant.okcredit.accounting.contract.model.LedgerType
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class PaymentBlindPayModule {

    companion object {
        @Provides
        fun initialState(fragment: PaymentBlindPayFragment): PaymentBlindPayContract.State {
            val maxDailyLimit = fragment.requireArguments().getLong(PaymentActivity.ARG_MAX_DAILY_LIMIT_EDT_PAGE, 0L)
            val remainingDailyLimit =
                fragment.requireArguments().getLong(PaymentActivity.ARG_REMAINING_DAILY_LIMIT_EDT_PAGE, 0L)
            val accountBalance = fragment.requireArguments().getLong(PaymentActivity.ARG_ACCOUNT_BALANCE, 0L)
            val merchantId = fragment.requireArguments().getString(PaymentActivity.ARG_MERCHANT_ID_EDT_PAGE, "")
            val riskType = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_RISK_TYPE, "LOW")
            val linkId = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_LINK_ID, "")
            val mobile = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_MOBILE, "")
            val paymentAddress = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_PAYMENT_ADDRESS, "")
            val destinationType =
                fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_DESTINATION_TYPE, "")
            val name = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_NAME, "")
            val accountId = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_ACCOUNT_ID, "")
            val accountType = fragment.requireArguments().getString(PaymentActivity.ARG_ACCOUNT_TYPE, "")
            val profileImage = fragment.requireArguments().getString(PaymentActivity.ARG_PAYMENT_PROFILE_IMAGE, "")
            val profileName = fragment.requireArguments().getString(PaymentActivity.ARG_PROFILE_NAME, "")
            return PaymentBlindPayContract.State().copy(
                maxDailyLimit = maxDailyLimit,
                remainingDailyLimit = remainingDailyLimit,
                dueBalance = accountBalance,
                merchantId = merchantId,
                riskType = riskType,
                accountId = accountId,
                linkId = linkId,
                mobile = mobile,
                paymentAddress = paymentAddress,
                destinationType = destinationType,
                name = name,
                accountType = accountType,
                profileImage = profileImage,
                profileName = profileName,
            )
        }

        @Provides
        @ViewModelParam(PaymentBlindPayContract.ARG_ACCOUNT_TYPE)
        fun accountType(paymentBlindPayFragment: PaymentBlindPayFragment): LedgerType {
            return LedgerType.valueOf(paymentBlindPayFragment.arguments?.get(PaymentActivity.ARG_ACCOUNT_TYPE) as String)
        }

        @Provides
        @ViewModelParam(PaymentBlindPayContract.ARG_ACCOUNT_ID)
        fun accountId(paymentBlindPayFragment: PaymentBlindPayFragment): String {
            return paymentBlindPayFragment.arguments?.get(PaymentActivity.ARG_PAYMENT_ACCOUNT_ID) as String
        }

        @Provides
        fun viewModel(
            paymentBlindPayFragment: PaymentBlindPayFragment,
            viewModelProvider: Provider<PaymentBlindPayViewModel>,
        ): MviViewModel<PaymentBlindPayContract.State> = paymentBlindPayFragment.createViewModel(viewModelProvider)
    }
}
