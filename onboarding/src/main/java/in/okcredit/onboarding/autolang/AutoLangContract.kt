package `in`.okcredit.onboarding.autolang

import `in`.okcredit.onboarding.contract.autolang.Language
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface AutoLangContract {

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val verifySuccess: Boolean = false,
        val appLockAbUIVariant: String? = null,
        val mobileNumber: String = "",
        val languages: List<Language>? = null,
        val isTrueCallerInstalled: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object StopLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object VerifySuccess : PartialState()

        object ClearNetworkError : PartialState()

        data class SetAppLockABVariant(val uiAbVariant: String) : PartialState()

        data class SetMobileNumber(val mobileNumber: String) : PartialState()

        data class SetLanguages(val languages: List<Language>) : PartialState()

        data class SetTrueCallerInstalled(val isTrueCallerInstalled: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        data class SetFreshLoginPref(val value: Boolean) : Intent()

        data class LanguageSelected(val selectedLanguage: String, val isSetFromBottomSheet: Boolean) : Intent()

        data class CheckMobileStatus(val mobileNumber: String) : Intent()

        data class TrueCallerLogin(val payload: String, val signature: String) : Intent()

        data class SubmitMobile(val text: String) : Intent()

        object NumberReadPopUp : Intent()

        // show loading
        data class LoadingState(val isShown: Boolean) : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {

        object GoToLogin : ViewEvent()

        object GoToSyncDataScreen : ViewEvent()

        object GoBack : ViewEvent()

        object GoToAppLockAuthentication : ViewEvent()

        data class ShowTrueCallerDialog(val isTrueCallerInstalled: Boolean) : ViewEvent()

        data class GoToOtpScreen(val mobile: String) : ViewEvent()

        data class SetLanguage(val language: Language) : ViewEvent()

        data class ShowLanguageSelector(
            val languages: List<Language>,
            val launchPhoneNumberFlowAfter: Boolean
        ) : ViewEvent()

        object GoToEnterNameScreen : ViewEvent()
    }
}
