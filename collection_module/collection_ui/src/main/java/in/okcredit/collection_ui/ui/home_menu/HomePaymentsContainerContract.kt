package `in`.okcredit.collection_ui.ui.home_menu

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface HomePaymentsContainerContract {

    data class State(
        val loading: Boolean = true,
        val showEmptyCollections: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetLoading(val loading: Boolean) : PartialState()

        data class EmptyCollections(val emptyCollections: Boolean) : PartialState()

        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object RefreshMerchantPayments : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowTransactionHistory : ViewEvent()
    }
}
