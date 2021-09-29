package `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet.di

import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_BALANCE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_ACCOUNT_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_DESTINATION_UPDATE_ALLOWED
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_FUTURE_AMOUNT_LIMIT
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_KYC_RISK_CATEGORY
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_KYC_STATUS
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_MAX_DAILY_LIMIT_EDT_PAGE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_MERCHANT_ID_EDT_PAGE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_ACCOUNT_ID
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_DESTINATION_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_LINK_ID
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_MOBILE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_NAME
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_PAYMENT_ADDRESS
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_PAYMENT_RISK_TYPE
import `in`.okcredit.payment.PaymentActivity.Companion.ARG_REMAINING_DAILY_LIMIT_EDT_PAGE
import `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet.PaymentEditAmountBottomSheet
import `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet.PaymentEditAmountContract
import `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet.PaymentEditAmountViewModel
import `in`.okcredit.shared.base.MviViewModel
import dagger.Module
import dagger.Provides
import merchant.okcredit.accounting.contract.model.LedgerType
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Provider

@Module
abstract class PaymentEditAmountModule {

    companion object {

        @Provides
        fun initialState(fragment: PaymentEditAmountBottomSheet): PaymentEditAmountContract.State {
            val maxDailyLimit = fragment.requireArguments().getLong(ARG_MAX_DAILY_LIMIT_EDT_PAGE, 0L)
            val remainingDailyLimit = fragment.requireArguments().getLong(ARG_REMAINING_DAILY_LIMIT_EDT_PAGE, 0L)
            val accountBalance = fragment.requireArguments().getLong(ARG_ACCOUNT_BALANCE, 0L)
            val merchantId = fragment.requireArguments().getString(ARG_MERCHANT_ID_EDT_PAGE, "")
            val riskType = fragment.requireArguments().getString(ARG_PAYMENT_RISK_TYPE, "LOW")
            val linkId = fragment.requireArguments().getString(ARG_PAYMENT_LINK_ID, "")
            val mobile = fragment.requireArguments().getString(ARG_PAYMENT_MOBILE, "")
            val paymentAddress = fragment.requireArguments().getString(ARG_PAYMENT_PAYMENT_ADDRESS, "")
            val destinationType = fragment.requireArguments().getString(ARG_PAYMENT_DESTINATION_TYPE, "")
            val name = fragment.requireArguments().getString(ARG_PAYMENT_NAME, "")
            val accountId = fragment.requireArguments().getString(ARG_PAYMENT_ACCOUNT_ID, "")
            val accountType = fragment.requireArguments().getString(ARG_ACCOUNT_TYPE, "")
            val kycStatus = KycStatus.valueOf(fragment.requireArguments().getString(ARG_KYC_STATUS, ""))
            val kycRiskCategory =
                KycRiskCategory.valueOf(fragment.requireArguments().getString(ARG_KYC_RISK_CATEGORY, ""))
            val futureAmountLimit = fragment.requireArguments().getLong(ARG_FUTURE_AMOUNT_LIMIT, 0L)
            val destinationUpdateAllowed = fragment.requireArguments().getBoolean(ARG_DESTINATION_UPDATE_ALLOWED, false)

            return PaymentEditAmountContract.State().copy(
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
                kycStatus = kycStatus,
                kycRiskCategory = kycRiskCategory,
                futureAmountLimit = futureAmountLimit,
                destinationUpdateAllowed = destinationUpdateAllowed,
            )
        }

        @Provides
        @ViewModelParam(PaymentEditAmountContract.ARG_ACCOUNT_TYPE)
        fun accountType(paymentEditAmountBottomSheet: PaymentEditAmountBottomSheet): LedgerType {
            return LedgerType.valueOf(paymentEditAmountBottomSheet.arguments?.get(ARG_ACCOUNT_TYPE) as String)
        }

        @Provides
        @ViewModelParam(PaymentEditAmountContract.ARG_ACCOUNT_ID)
        fun accountId(paymentEditAmountBottomSheet: PaymentEditAmountBottomSheet): String {
            return paymentEditAmountBottomSheet.arguments?.get(ARG_PAYMENT_ACCOUNT_ID) as String
        }

        @Provides
        fun viewModel(
            fragment: PaymentEditAmountBottomSheet,
            viewModelProvider: Provider<PaymentEditAmountViewModel>,
        ): MviViewModel<PaymentEditAmountContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
