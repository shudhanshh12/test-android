package tech.okcredit.applock.enterPin

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface EnterPinContract {
    data class State(
        val loading: Boolean = false,
        val incorrectPin: Boolean = false,
        val pin: String = "",
        val isFingerPrintEnabled: Boolean = false,
        val isFingerprintEnrolledInDevice: Boolean = false,
        val source: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : EnterPinContract.PartialState()
        data class SetIncorrectPin(val incorrectPin: Boolean) : EnterPinContract.PartialState()
        data class SetPin(val pin: String) : EnterPinContract.PartialState()
        data class SetFingerEnrolled(val isFingerprintEnrolledInDevice: Boolean) : EnterPinContract.PartialState()
        data class SetFingerprintEnabled(val enabledOrNot: Boolean) : PartialState()
        data class SetSource(val source: String) : PartialState()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GotoHomeScreen : ViewEvent()
        data class Authenticated(val pin: String) : ViewEvent()
        object AuthError : ViewEvent()
        object InternetError : ViewEvent()
        object ShowMessageWithRetry : ViewEvent()
        object GoToSetPinScreen : ViewEvent()
        data class ShowInputMode(val isShowFingerprint: Boolean) : ViewEvent()
    }

    sealed class Intent : UserIntent {
        object Load : EnterPinContract.Intent()
        object ForgotPin : EnterPinContract.Intent()
        object CheckInputMode : EnterPinContract.Intent()
        data class VerifyPin(val pin: String) : EnterPinContract.Intent()
    }
}
