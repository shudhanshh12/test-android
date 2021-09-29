package `in`.okcredit.onboarding.otp_verification

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface OtpVerificationContract {

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val networkErrorWithRetry: Boolean = false,
        val isShowResendOtp: Boolean = false,
        val sendOtpLoader: Boolean = false,
        val otpError: Boolean = false,
        val mobile: String = "",
        val flag: Int = 0,
        val resendOtpLoader: Boolean = false

    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object HideLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        data class SetMobile(val mobile: String) : PartialState()

        data class SetFlag(val flag: Int) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetNetworkErrorWithRetry(val networkErrorWithRetry: Boolean) : PartialState()

        data class SetResendOtpVisibility(val status: Boolean) : PartialState()

        data class SetInCorrectOtpStatus(val status: Boolean) : PartialState()

        data class SetSendOtpLoadingStatus(val status: Boolean) : PartialState()

        object ReSetDefaultOtpView : PartialState()

        object ClearOtpError : PartialState()

        object ClearNetworkError : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        object ResendOtp : Intent()

        data class ShowAlert(val message: String) : Intent()

        data class VerifyOtp(val isAutoRead: Boolean, val otp: String) : Intent()
    }

    interface Navigator {
        fun gotoLogin()

        fun goBack()

        fun goHome()

        fun goBackWithSuccessResult()
    }
}
