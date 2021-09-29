package tech.okcredit.applock.changePin

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface ChangeSecurityPinContract {

    data class State(
        val isLoading: Boolean = true,
        val mobile: String = "",
        val otpSent: Boolean? = null,
        val incorrectOtp: Boolean = false,
        val errorMessage: Int? = null,
        val verificationInProgress: Boolean = false,
        val isUpdatePassword: Boolean = false,
        val source: String = "",
        val entry: String = "",
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetMobilenumber(val mobile: String) : PartialState()
        object SendOtpSuccess : PartialState()
        data class SendOtpFailure(@StringRes val message: Int) : PartialState()
        data class VerifyOtpFailure(@StringRes val message: Int) : PartialState()
        object Verifying : PartialState()
        data class SetUpdatePassword(val isUpdatePassword: Boolean) : PartialState()
        data class SetSourceAndEntry(val source: String, val entry: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        data class VerifyOtp(val otpString: String) : Intent()
        object SendOtpClick : Intent()

        object ResendOtp : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object SendOtpSuccess : ViewEvent()

        data class VerifyNetworkError(@StringRes val errorMessage: Int) : ViewEvent()

        data class SendOtpError(@StringRes val errorMessage: Int) : ViewEvent()

        data class Toast(@StringRes val resId: Int) : ViewEvent()

        data class SendOtp(val mobile: String) : ViewEvent()

        object GoToSetPinScreen : ViewEvent()
    }
}
