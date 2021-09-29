package tech.okcredit.home.ui.settings

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface SettingsContract {

    data class State(
        val isLoading: Boolean = true,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val activeLangugeStringId: Int = -1,
        val appLockType: String = "",
        val isAppLockActive: Boolean = false,
        val isSetPassword: Boolean = false,
        val isPaymentPasswordEnabled: Boolean = false,
        val signOut: Boolean = false,
        val isFingerPrintLockVisible: Boolean = false,
        val isFingerPrintEnabled: Boolean = false,
        val isFourDigitPinSet: Boolean = false,
        val isMerchantPrefSync: Boolean = false,
        val isPspUpiFeatureEnabled: Boolean = false,
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        data class SetActiveLanguage(val activeLangugeStringId: Int) : PartialState()

        data class SetIsPasswordEnabled(val isSetPassword: Boolean) : PartialState()

        data class SetPaymentPasswordEnabled(val isPaymentPasswordEnabled: Boolean) : PartialState()

        data class SetAppLockType(val isAppLockActive: Boolean, val appLockType: String) : PartialState()

        object SetSignout : PartialState()

        data class SetFingerPrintVisible(val isFingerPrintLockVisible: Boolean) : PartialState()

        data class SetFingerprintEnabled(val isFingerPrintEnabled: Boolean) : PartialState()

        data class SetIsFourDigitPin(val isFourDigitPinSet: Boolean) : PartialState()

        data class SetIsMerchantPrefSync(val isMerchantPrefSync: Boolean) : PartialState()

        data class SetIsPspUpiEnabled(val isEnabled: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // screen onResume
        object Resume : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        object ProfileClick : Intent()

        object AppLanguageClick : Intent()

        object AppLockClick : Intent()

        object PaymentPasswordClick : Intent()

        object SignOutFromAllDevices : Intent()

        object SignoutConfirmationClick : Intent()

        data class UpdatePasswordClick(val isSetPassword: Boolean) : Intent()

        object SignOut : Intent()

        object ChangeNumberClick : Intent()

        data class SetNewPin(val type: SettingsClicks) : Intent()

        data class UpdatePin(val type: SettingsClicks) : Intent()

        data class SetFingerPrintEnable(val fingerprintEnable: Boolean) : Intent()

        data class SyncMerchantPref(val type: SettingsClicks) : Intent()

        data class CheckIsFourDigit(val type: SettingsClicks) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object gotoLogin : ViewEvent()

        object GoToProfileScreen : ViewEvent()

        object GoToLanguageScreen : ViewEvent()

        data class GotoResetPasswordScreen(val mobile: String, val isSetPassword: Boolean) : ViewEvent()

        object OpenAppLock : ViewEvent()

        object OpenLogoutConfirmationDialog : ViewEvent()

        object GoToPaymentPasswordEnableScreen : ViewEvent()

        object ShowInvalidPassword : ViewEvent()

        object GoToChangeNumberScreen : ViewEvent()

        data class GoToSetNewPinScreen(val type: SettingsClicks) : ViewEvent()

        data class ShowUpdatePinDialog(val type: SettingsClicks) : ViewEvent()

        data class SyncDone(val type: SettingsClicks) : ViewEvent()

        data class CheckFourDigitPinDone(val type: SettingsClicks, val isFourDigitPinSet: Boolean) : ViewEvent()
    }
}
