package `in`.okcredit.payment.ui.payment_blind_pay

import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.utils.getFinalDueAmount
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.contract.model.SupportType

interface PaymentBlindPayContract {
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
        val profileImage: String = "",
        val profileName: String = "",
        val supportType: SupportType = SupportType.NONE,
        val supportNumber: String = "",
        val support24x7String: String = "",
    ) : UiState {

        fun getPaymentPrefillBalance(): Long {
            return getFinalDueAmount(dueBalance, remainingDailyLimit)
        }

        fun getRelationFrmAccountType(): String {
            return if (accountType == LedgerType.SUPPLIER.value) PaymentAnalyticsEvents.PaymentPropertyValue.SUPPLIER
            else PaymentAnalyticsEvents.PaymentPropertyValue.CUSTOMER
        }

        fun isSupplier(): Boolean = (accountType == LedgerType.SUPPLIER.value)
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetAmountEntered(val amount: Long) : PartialState()
        data class SetSupportData(
            val supportType: SupportType,
            val supportNumber: String,
            val support24x7String: String,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class SetAmountEntered(val amount: Long) : Intent()
        data class SupportClicked(val msg: String, val number: String) : Intent()
        data class SendWhatsAppMessage(val msg: String, val number: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        data class ShowToast(val msg: String) : ViewEvents()
        data class TrackPageSummaryEvent(val type: String, val number: String) : ViewEvents()
        object ShowWhatsAppError : ViewEvents()
        object ShowDefaultError : ViewEvents()
        data class SendWhatsAppMessage(val intent: android.content.Intent) : ViewEvents()
        object CallCustomerCare : ViewEvents()
    }

    companion object {
        const val ARG_ACCOUNT_TYPE = "account_type"
        const val ARG_ACCOUNT_ID = "account_id"
    }
}
