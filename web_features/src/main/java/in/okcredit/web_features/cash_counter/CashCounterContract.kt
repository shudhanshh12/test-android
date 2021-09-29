package `in`.okcredit.web_features.cash_counter

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CashCounterContract {

    data class State(
        val isLoading: Boolean = true,
        val merchantId: String = "",
        val authToken: String = ""
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()
        object WebPageLoaded : Intent()
        data class ShowError(val error: String) : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        object WebPageLoaded : PartialState()

        data class SetMerchantIdAndAuthToken(val merchantId: String, val authToken: String) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        object WebPageLoaded : ViewEvent()
        object ShowError : ViewEvent()
    }
}
