package `in`.okcredit.collection_ui.ui.home

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CollectionsHomeActivityContract {

    data class State(
        val loading: Boolean = true,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetLoading(val loading: Boolean) : PartialState()

        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class CollectionAdoption(val referralMerchantId: String?) : ViewEvent()

        object QrScreen : ViewEvent()
    }
}
