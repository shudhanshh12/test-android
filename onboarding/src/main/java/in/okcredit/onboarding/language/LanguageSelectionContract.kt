package `in`.okcredit.onboarding.language

import `in`.okcredit.onboarding.contract.autolang.Language
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface LanguageSelectionContract {

    data class State(
        val isLoading: Boolean = false,
        val selectedLanguage: String = "",
        val languages: List<Language>? = null,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        object ShowLoading : PartialState()

        object StopLoading : PartialState()

        data class SetLanguages(
            val selectedLanguage: String,
            val languages: List<Language>,
        ) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object OnResume : Intent()

        data class LanguageSelected(val selectedLanguage: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object GoToEnterPhoneNumber : ViewEvent()

        object GoToWelcomeSocialValidation : ViewEvent()
    }
}
