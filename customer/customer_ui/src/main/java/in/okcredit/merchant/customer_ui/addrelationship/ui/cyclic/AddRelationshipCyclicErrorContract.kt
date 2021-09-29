package `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AddRelationshipCyclicErrorContract {
    data class State(
        val name: String = "",
        val mobile: String = "",
        val profile: String? = null,
        val relationshipId: String? = null,
        val viewRelationshipType: Int? = null,
        val moveRelationshipType: Int? = null,
        val descriptionText: String = "",
        val headerText: String = "",
        val canShowMoveCta: Boolean = false,
        val shouldRequireReactivation: Boolean = false,
        val typeOfConflict: String = "",
        val source: String = "",
        val defaultMode: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object MoveToSupplier : Intent()
        object MoveToCustomer : Intent()
        object TrackViewConflictDialog : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class GotoMoveToSupplierFlow(val supplierId: String?) : ViewEvent()
    }
}
