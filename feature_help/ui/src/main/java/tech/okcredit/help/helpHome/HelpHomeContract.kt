package tech.okcredit.help.helpHome

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface HelpHomeContract {

    data class State(
        val sourceScreen: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class SetSourceScreen(val source: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object ClickHelp : Intent()

        object AboutUsClick : Intent()

        object PrivacyClick : Intent()

        object ChatWithUsClick : Intent()

        data class OnWhatsAppPermissionCheck(val isWhatsAppContactPermissionEnabled: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class GoToHelp(val sourceScreen: String) : ViewEvent()

        object GoToAboutUsScreen : ViewEvent()

        object GoToPrivacyScreen : ViewEvent()

        object GoToManualChatScreen : ViewEvent()

        object CheckWhatsAppPermission : ViewEvent()

        object GoToWhatsAppScreen : ViewEvent()

        data class OpenWhatsApp(val mobile: String) : ViewEvent()
    }
}
