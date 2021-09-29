package tech.okcredit.feedback.feedback

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface FeedbackContract {

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ClearNetworkError : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        data class SubmitFeedback(val feedbackMessage: String) : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GotoLogin : ViewEvent()

        object GoBack : ViewEvent()

        object GoBackAfterAnimation : ViewEvent()
    }
}
