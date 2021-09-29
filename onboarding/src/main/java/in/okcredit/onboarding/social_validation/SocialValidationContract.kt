package `in`.okcredit.onboarding.social_validation

import `in`.okcredit.onboarding.social_validation.data.SocialValidationPage
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface SocialValidationContract {

    data class State(
        val isLoading: Boolean = false,
        val pages: List<SocialValidationPage> = emptyList(),
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        object ShowLoading : PartialState()

        object StopLoading : PartialState()

        data class SetPages(val pages: List<SocialValidationPage>) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object GetStarted : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object GoToEnterPhoneNumber : ViewEvent()
    }
}
