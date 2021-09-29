package `in`.okcredit.frontend.ui.applock

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AppLockContract {

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val exit: String? = null,
        val source: String? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class CurrentSource(val source: String) : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ClearNetworkError : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        data class ExitScreen(val exit: String) : Intent()

        data class AppLockEnabled(val exit: String) : Intent()

        data class AppLockAuthenticated(val userFlow: String) : Intent()

        object TurnOffLock : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object GoToLogin : ViewEvent()

        object AuthenticateAppResume : ViewEvent()

        object AuthenticateAppResumeDeeplink : ViewEvent()

        object SetupLockFromSettingScreen : ViewEvent()

        object AuthenticateAndTurnOffLockFromSettingScreen : ViewEvent()

        object SetupLockMixpanelInAppNotiFlow : ViewEvent()

        object SetupLockForExistingUserLoginFlow : ViewEvent()

        data class EXIT(val exitType: String) : ViewEvent()
    }
}
