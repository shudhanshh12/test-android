package `in`.okcredit.frontend.ui.number_change

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface InfoChangeNumberContract {

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val mobile: String = ""
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetLoaderStatus(val status: Boolean) : PartialState()

        data class Number(val mobile: String) : PartialState()

        object ClearNetworkError : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        object VerifyAndChange : Intent()
    }

    interface Navigator {

        fun goBack()

        fun goToOTPVerificationScreen(mobile: String)
    }
}
