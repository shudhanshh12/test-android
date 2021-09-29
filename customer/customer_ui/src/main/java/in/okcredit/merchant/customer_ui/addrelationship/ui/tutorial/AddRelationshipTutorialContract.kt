package `in`.okcredit.merchant.customer_ui.addrelationship.ui.tutorial

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AddRelationshipTutorialContract {
    data class State(
        val isLoading: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent
}
