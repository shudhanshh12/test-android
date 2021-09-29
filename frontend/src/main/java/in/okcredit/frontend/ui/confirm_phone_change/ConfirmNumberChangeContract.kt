package `in`.okcredit.frontend.ui.confirm_phone_change

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface ConfirmNumberChangeContract {

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val business: Business? = null,
        val tempNewNumber: String? = null
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

        data class MerchantStats(val business: Business, val tempNewNumber: String) : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object VerfiyAndChange : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()
    }

    interface Navigator {

        fun goBack()

        fun goToOTPVerificationScreen(mobile: String)
    }
}
