package `in`.okcredit.collection_ui.ui.home.adoption

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CollectionAdoptionV2Contract {
    data class State(
        val loading: Boolean = true,
        val referredByMerchantId: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object SetupClicked : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class GoToAddDestination(val referralMerchantId: String?) : ViewEvent()
    }
}
