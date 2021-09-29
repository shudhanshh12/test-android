package tech.okcredit.applock.pinLock

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface PinLockContract {
    data class State(
        val pinValue: String = "",
        val isIncorrectPin: Boolean = false,
        val isUpdatePin: Boolean = false,
        val source: String = "",
        val entry: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetPinValue(val pinValue: String) : PartialState()
        data class SetIsUpdatePin(val isUpdatePin: Boolean) : PartialState()
        data class SetSourceAndEntry(val source: String, val entry: String) : PartialState()
        object SetIncorrectPin : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class SetPin(val pinValue: String) : Intent()
        data class ConfirmPin(val pinValue: String, val confirmPin: String) : Intent()
        object FourDigitPinUpdated : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object AskConfirmPin : ViewEvent()
        object PinVerified : ViewEvent()
        object UpdateMerchantPref : ViewEvent()
        object ShowIncorrectPin : ViewEvent()
        data class ShowError(val errRes: Int) : ViewEvent()
    }
}
