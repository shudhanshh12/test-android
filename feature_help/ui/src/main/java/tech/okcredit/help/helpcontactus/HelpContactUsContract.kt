package tech.okcredit.help.helpcontactus

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface HelpContactUsContract {

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val isManualChatEnabled: Boolean = false,
        val sourceScreen: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        data class SetSourceScreen(val source: String) : PartialState()

        data class SetManualChatEnabled(val isManualChatEnabled: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object ContactUs : Intent()

        object EmailUs : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        data class WhatsApp(val contactPermissionAvailable: Boolean) : Intent()
    }

    interface Navigator {
        fun gotoLogin()

        fun goBack()

        fun onContactUsClicked()

        fun onEmailClicked()

        fun openWhatsApp(helpNumber: String)

        fun goToWhatsAppOptIn()

        fun goToManualChatScreen()
    }
}
