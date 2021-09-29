package merchant.okcredit.gamification.ipl.view

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface IplContract {

    data class State(
        val isLoading: Boolean = true,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object EducationViewed : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object ShowEducation : ViewEvent()
    }
}
