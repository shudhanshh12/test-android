package `in`.okcredit.collection_ui.ui.passbook.refund

import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface RefundConsentContract {

    data class State(
        val error: String = "",
        val payoutId: String = "",
        val txnId: String = "",
        val showLoader: Boolean = false,
        val currentStatus: Int = OnlinePaymentsContract.PaymentStatus.PAYOUT_FAILED.value,
        val paymentId: String = "",
        val collectionType: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        object StartLoader : PartialState()
        object StopLoader : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object InitiateRefund : Intent()
        object Cancel : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {
        object RefundSuccessful : ViewEvents()
        data class ShowError(val message: String) : ViewEvents()
        object Cancel : ViewEvents()
    }
}
