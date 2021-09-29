package `in`.okcredit.merchant.customer_ui.addrelationship

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AddRelationshipContract {
    object State : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class GoToAddRelationshipFromContacts(
            val relationshipType: Int,
            val source: String,
            val openForResult: Boolean,
        ) : ViewEvent()

        data class GoToAddRelationshipManually(
            val relationshipType: Int,
            val source: String,
            val openForResult: Boolean,
        ) : ViewEvent()

        data class GoToAddRelationshipTutorial(val relationshipType: Int) : ViewEvent()
    }
}
