package `in`.okcredit.merchant.customer_ui.ui.updatetransactionamount

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface UpdateTransactionAmountContract {

    data class State(
        val isLoading: Boolean = false,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val error: Boolean = false,
        val networkError: Boolean = false,
        val transactionAmount: Long = 0,
        val transaction: merchant.okcredit.accounting.model.Transaction? = null,
        val customer: Customer? = null
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object HideLoading : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetTransaction(val transaction: merchant.okcredit.accounting.model.Transaction) : PartialState()

        data class SetCustomer(val customer: Customer) : PartialState()

        object ClearNetworkError : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        object NoChange : Intent()

        // show alert
        data class ShowAlert(val message: String) : Intent()

        data class UpdateTransactionAmount(val transactionAmount: Long) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        object GoToLogin : ViewEvent()

        object AmountUpdatedSuccessfully : ViewEvent()
    }
}
