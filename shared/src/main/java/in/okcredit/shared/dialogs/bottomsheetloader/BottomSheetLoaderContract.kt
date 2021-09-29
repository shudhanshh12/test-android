package `in`.okcredit.shared.dialogs.bottomsheetloader

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface BottomSheetLoaderContract {

    data class State(
        val isLoading: Boolean = true,
        val result: Boolean? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        object ShowLoading : PartialState()
        object ShowSuccess : PartialState()
        object ShowFailure : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object Success : Intent()
        object Fail : Intent()
        object Dismiss : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object Dismiss : ViewEvent()
    }
}
