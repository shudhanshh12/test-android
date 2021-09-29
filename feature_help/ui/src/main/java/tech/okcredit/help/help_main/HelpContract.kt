package tech.okcredit.help.help_main

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.userSupport.model.Help

interface HelpContract {

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val help: List<Help>? = null,
        val expandedId: String = "",
        val sourceScreen: String = "",
        val isExpanded: Boolean = false,
        val isFromContextualHelp: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetHelpData(val helpList: List<Help>, val expand: Boolean = false) : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        data class ExpandedId(val expandedId: String, val isExpanded: Boolean) : PartialState()

        data class SetSourceScreen(val source: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // main item click
        data class MainItemClick(val secId: String, val isExpanded: Boolean) : Intent()

        // section item click
        data class OnSectionItemClick(val secId: String) : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        object ChatWithUsClick : Intent()

        // section item click
        data class OnWhatsAppPermissionCheck(val isWhatsAppContactPermissionEnabled: Boolean) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        data class GotoHelpItem(val secId: String) : ViewEvent()

        object GoBack : ViewEvent()

        object GoToManualChatScreen : ViewEvent()

        object CheckWhatsAppPermission : ViewEvent()

        data class OpenWhatsApp(val helpNumber: String) : ViewEvent()

        object GoToWhatsAppOptIn : ViewEvent()

        object OpenDefaultFaq : ViewEvent()
    }
}
