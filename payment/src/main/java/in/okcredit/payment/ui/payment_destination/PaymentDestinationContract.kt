package `in`.okcredit.payment.ui.payment_destination

import `in`.okcredit.payment.server.internal.PaymentDestinationType
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface PaymentDestinationContract {

    data class State(
        val adoptionMode: String = PaymentDestinationType.BANK.value,
        val showUi: UiScreenType = UiScreenType.BANK,
        val accountHolderName: String = "",
        val enteredAccountNumber: String = "",
        val enteredIfsc: String = "",
        val enteredUpi: String = "",
        val showUpiOption: Boolean = false,
        val showDescText: Boolean = false
    ) : UiState

    enum class UiScreenType {
        BANK,
        UPI,
        VALIDATE,
        SUCCESS
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()

        data class EnteredAccountNumber(val enteredAccountNumber: String) : PartialState()

        data class EnteredIfsc(val enteredIfsc: String) : PartialState()

        data class EnteredUpi(val enteredUPI: String) : PartialState()

        data class ShowSuccessUi(val accountHolderName: String) : PartialState()

        object ShowValidateUi : PartialState()

        object ShowBankUi : PartialState()

        object ShowUpiUi : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class SetDestinationToServer(val paymentAddress: String, val paymentType: String) : Intent()

        data class SetAdoptionMode(val adoptionMode: String) : Intent()

        data class EnteredAccountNumber(val enteredAccountNumber: String) : Intent()

        data class EnteredIfsc(val enteredIfsc: String) : Intent()

        data class EnteredUPI(val enteredUPI: String) : Intent()
    }

    sealed class ViewEvents : BaseViewEvent {

        object GoToLogin : ViewEvents()

        object OnAccountAddedSuccessfully : ViewEvents()

        data class ShowError(val errMsg: String) : ViewEvents()
    }
}
