package merchant.okcredit.accounting.ui.customer_support_option_dialog

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import merchant.okcredit.accounting.contract.model.SupportType

interface CustomerSupportOptionContract {

    data class State(
        val supportType: SupportType = SupportType.NONE,
        val supportMsg: String = "",
        val accountId: String = "",
        val txnId: String = "",
        val amount: String = "",
        val isActionClicked: Boolean = false,
        val ledgerType: String = "",
        val supportChatNumber: String = "",
        val supportCallNumber: String = "",
        val support24x7String: String = "",
        val source: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetSupportData(
            val supportType: SupportType,
            val supportCallNumber: String,
            val supportChatNumber: String,
            val support24x7String: String,
        ) : PartialState()

        data class SetActionClicked(val clicked: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class SendWhatsAppMessage(val msg: String) : Intent()
        data class ActionCallClicked(val msg: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object ShowWhatsAppError : ViewEvents()
        object ShowDefaultError : ViewEvents()
        data class SendWhatsAppMessage(val intent: android.content.Intent) : ViewEvents()
        object CallCustomerCare : ViewEvents()
    }
}
