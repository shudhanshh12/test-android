package `in`.okcredit.onboarding.enterotp.v2

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import androidx.annotation.StringRes

interface OtpContractV2 {

    data class State(
        val otpSent: Boolean? = null,
        val incorrectOtp: Boolean = false,
        val errorMessage: Int? = null,
        val verificationInProgress: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object NoChange : PartialState()

        data class SendOtpFailure(@StringRes val message: Int) : PartialState()

        object SendOtpSuccess : PartialState()

        data class VerifyOtpFailure(@StringRes val message: Int) : PartialState()

        object Verifying : PartialState()
    }

    sealed class Intent : UserIntent {

        object Load : Intent()

        object ResendOtp : Intent()

        data class VerifyOtp(val otp: String, val isAutoRead: Boolean) : Intent()

        object EditMobile : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object GoToSyncDataScreen : ViewEvent()

        object GoToAppLockScreen : ViewEvent()

        object GoToNameScreen : ViewEvent()

        object GoToMobileScreen : ViewEvent()

        data class VerifyNetworkError(@StringRes val errorMessage: Int) : ViewEvent()

        data class SendOtpError(@StringRes val errorMessage: Int) : ViewEvent()

        data class Toast(@StringRes val resId: Int) : ViewEvent()

        object SendOtpSuccess : ViewEvent()
    }
}
