package `in`.okcredit.payment.ui.blindpay

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.model.SupportType

interface BlindPayContract {

    data class State(
        val supportType: SupportType = SupportType.NONE,
        val ledgerType: String = "",
        val supportNumber: String = "",
        val support24x7String: String = "",
        val accountId: String = "",
        val supportMsg: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetSupportData(
            val supportType: SupportType,
            val supportNumber: String,
            val support24x7String: String,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class SendWhatsAppMessage(val msg: String, val number: String) : Intent()
        data class SupportClicked(val msg: String, val number: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object ShowWhatsAppError : ViewEvents()
        object ShowDefaultError : ViewEvents()
        data class SendWhatsAppMessage(val intent: android.content.Intent) : ViewEvents()
        object CallCustomerCare : ViewEvents()
    }
}
