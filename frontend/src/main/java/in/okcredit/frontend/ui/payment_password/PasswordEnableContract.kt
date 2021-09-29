package `in`.okcredit.frontend.ui.payment_password

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface PasswordEnableContract {

    data class State(
        val isPasswordEnable: Boolean = false,
        val isEnterPasswordMode: Boolean = false,
        val password: String = "",
        val isIncorrectPassword: Boolean = false,
        val networkError: Boolean = false,
        val loader: Boolean = false,
        val isPasswordSet: Boolean = false,
        val isFourDigitPin: Boolean = false,
        val isMerchantSync: Boolean = false,
        val error: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {
        data class SetPasswordEnableStatus(val value: Boolean) : PartialState()

        data class ChangeScreenPassword(val isPasswordMode: Boolean) : PartialState()

        data class SetPasswordEnableErrorStatus(val status: Boolean) : PartialState()

        object NoChange : PartialState()

        object ShowLoader : PartialState()

        object HideLoader : PartialState()

        data class SetNetworkError(val status: Boolean) : PartialState()

        data class SetIsPasswordEnabled(val isPasswordSet: Boolean) : PartialState()

        data class SetIsFourdigitPin(val isFourDigitPin: Boolean) : PartialState()

        data class SetIsMerchantSync(val isMerchantSync: Boolean) : PartialState()

        object ErrorState : PartialState()
    }

    sealed class Intent : UserIntent {
        // show alert
        object Load : Intent()

        data class ChangePasswordEnableStatus(val isEnterPasswordMode: Boolean) : Intent()

        data class SubmitPassword(val status: Boolean) : Intent()

        object UpdatePinClicked : Intent()

        object SetNewPinClicked : Intent()

        object SyncMerchantPref : Intent()

        // forgot password clicked
        object OnForgotPasswordClicked : Intent()

        object CheckIsFourDigit : Intent()
    }

    interface Navigator {
        fun gotoLogin()

        fun gotoForgotPasswordScreen(mobile: String)

        fun gotoEnterPinScreen()

        fun showUpdatePinDialog()

        fun showSetNewPinDialog()

        fun checkFourDigitPin(isFourDigit: Boolean)

        fun syncDone()

        fun goBack()
    }
}
