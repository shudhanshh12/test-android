package `in`.okcredit.onboarding.enterotp

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import tech.okcredit.android.auth.server.AuthApiClient.RequestOtpMedium

interface EnterOtpContract {

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
        val isForAuthFailure: Boolean = false,
        val resendOtpLoader: Boolean = false,
        val flow: String = "",
        val flag: Int = 0,
        val logout: Boolean = false,
        val canShowMigrationSuccessfulView: Boolean = false,
        val merchantAlreadyExistsError: Boolean = false,
        val verifiedSuccessfully: Boolean = false,
        val appLockAbUIVariant: String? = null,
        val pinOrOtpVariant: String? = null,
        val googlePopUp: Boolean = false,
        val mobileStatus: Boolean = false,
        val showFallbackOption: Boolean = false,
        val fallbackOptions: ArrayList<Int> = ArrayList(),
        val lastRetryMedium: RequestOtpMedium = RequestOtpMedium.SMS,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object HideLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        data class SetMobile(val mobile: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetNetworkErrorWithRetry(val networkErrorWithRetry: Boolean) : PartialState()

        data class SetResendOtpVisibility(val status: Boolean) : PartialState()

        data class SetInCorrectOtpStatus(val status: Boolean) : PartialState()

        object SetVerifiedSuccessfully : PartialState()

        data class SetSendOtpLoadingStatus(val status: Boolean) : PartialState()

        data class ShowMigrationSuccessfulView(val canShowMigrationSuccessfulView: Boolean) : PartialState()

        data class Flag(val flag: Int) : PartialState()

        object ReSetDefaultOtpView : PartialState()

        object ClearOtpError : PartialState()

        object ClearNetworkError : PartialState()

        object LogOut : PartialState()

        object SetMerchantExists : PartialState()

        data class SetAppLockABVariant(val uiAbVariant: String) : PartialState()

        data class SetPinOrOtpText(val variant: String) : PartialState()

        data class SetGooglePopUp(val googlePopUp: Boolean) : PartialState()

        data class MobileStatus(val mobileStatus: Boolean) : PartialState()

        object HideFallbackOptions : PartialState()

        object ShowFallbackOptions : PartialState()

        data class LastRetryMedium(val lastRetryMedium: RequestOtpMedium) : PartialState()

        data class FallbackOptions(val options: ArrayList<Int>) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object WaitingTimeFinished : Intent()

        object SendOtpViaSms : Intent()

        object SendOtpViaWhatsApp : Intent()

        object SendOtpViaIvr : Intent()

        object ResendOtp : Intent()

        data class OtpReadFailed(val reason: String) : Intent()

        object EnterMobile : Intent()

        data class ShowAlert(val message: String) : Intent()

        data class VerifyOtp(val isAutoRead: Boolean, val otp: String) : Intent()

        data class StartOtpFlowTimer(val overallProcessTime: Long) : Intent()

        object EndOtpProcess : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToLogin : ViewEvent()

        object GoToHome : ViewEvent()

        // This called when verifying otp
        // this will show lottie animation loader with auto verify success ui
        object GoToSyncDataScreen : ViewEvent()

        object GoToEnterNameScreen : ViewEvent()

        object GoToChangeNumberScreen : ViewEvent()

        data class GoBackWithError(val error: String) : ViewEvent()

        object GoToAppLockAuthentication : ViewEvent()

        object GoToEnterMobileScreen : ViewEvent()

        object TooManyRequests : ViewEvent()

        data class SendOtpSuccess(val requestOtpMedium: RequestOtpMedium) : ViewEvent()

        object ResetCompleteOtpFlow : ViewEvent()

        data class StartTimer(val retryWaitingTime: Long) : ViewEvent()
    }
}
