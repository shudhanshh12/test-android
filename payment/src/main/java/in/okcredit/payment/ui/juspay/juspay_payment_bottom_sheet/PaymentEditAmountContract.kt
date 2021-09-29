package `in`.okcredit.payment.ui.juspay.juspay_payment_bottom_sheet

import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.CUSTOMER
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents.PaymentPropertyValue.SUPPLIER
import `in`.okcredit.payment.usecases.KycBannerType
import `in`.okcredit.payment.utils.getFinalDueAmount
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.model.SupportType

interface PaymentEditAmountContract {

    data class State(
        val dueBalance: Long = 0L,
        val maxDailyLimit: Long = 0L,
        val remainingDailyLimit: Long = 0L,
        val currentAmountSelected: Long? = null,
        val merchantId: String = "",
        val riskType: String = "",
        val accountId: String = "",
        val linkId: String = "",
        val mobile: String = "",
        val paymentAddress: String = "",
        val destinationType: String = "",
        val name: String = "",
        val accountType: String = "",
        val cashbackMessage: String? = null,
        val kycStatus: KycStatus = KycStatus.NOT_SET,
        val kycRiskCategory: KycRiskCategory = KycRiskCategory.NO_RISK,
        val kycBannerType: KycBannerType = KycBannerType.None,
        val shouldShowCreditCardInfoForKyc: Boolean = false,
        val futureAmountLimit: Long = 0L,
        val supportType: SupportType = SupportType.NONE,
        val supportNumber: String = "",
        val support24x7String: String = "",
        val destinationUpdateAllowed: Boolean = true,
    ) : UiState {

        fun getPaymentPrefillBalance(): Long {
            return getFinalDueAmount(dueBalance, remainingDailyLimit)
        }

        fun getRelationFrmAccountType(): String {
            return if (accountType == LedgerType.SUPPLIER.value) SUPPLIER
            else CUSTOMER
        }
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetAmountEntered(val amount: Long) : PartialState()
        data class SetShouldShowCreditCardInfoForKyc(val shouldShowCreditCardInfoForKyc: Boolean) : PartialState()
        data class SetCashbackMessage(val cashbackMessage: String) : PartialState()
        data class SetKycBannerType(val kycBannerType: KycBannerType) : PartialState()
        data class SetSupportData(
            val supportType: SupportType,
            val supportNumber: String,
            val support24x7String: String,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class SetAmountEntered(val amount: Long) : Intent()
        object KycEntryPointClicked : Intent()
        object CloseKycInfoBanner : Intent()
        data class SupportClicked(val msg: String, val number: String) : Intent()
        data class SendWhatsAppMessage(val msg: String, val number: String) : Intent()
        data class TrackPageSummaryEvent(val type: String, val number: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object GoToLogin : ViewEvents()
        object GoToKycWebScreen : ViewEvents()
        data class ShowToast(val msg: String) : ViewEvents()
        data class TrackPageSummaryEvent(val type: String, val number: String) : ViewEvents()
        object CallCustomerCare : ViewEvents()
        object ShowWhatsAppError : ViewEvents()
        object ShowDefaultError : ViewEvents()
        data class SendWhatsAppMessage(val intent: android.content.Intent) : ViewEvents()
    }

    companion object {
        const val ARG_ACCOUNT_TYPE = "account_type"
        const val ARG_ACCOUNT_ID = "account_id"
    }
}
