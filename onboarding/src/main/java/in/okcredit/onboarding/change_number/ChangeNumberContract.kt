package `in`.okcredit.onboarding.change_number

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ChangeNumberContract {

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val merchantAlreadyExistsError: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetLoaderStatus(val status: Boolean) : PartialState()

        object ClearNetworkError : PartialState()

        object SetMerchantExists : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        data class NewNumberEntered(val newNumber: String) : Intent()
    }

    interface Navigator {

        fun goBack()

        fun gotoLogin()

        fun goToOTPVerificationScreen(mobile: String)

        fun goToChangeNumberConfirmationScreen()
    }
}
