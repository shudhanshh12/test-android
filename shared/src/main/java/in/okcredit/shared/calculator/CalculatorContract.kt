package `in`.okcredit.shared.calculator

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface CalculatorContract {
    data class State(
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val amountCalculation: String? = null,
        val calculatorOperatorsUsed: String? = null,
        val amount: Long = 0L,
        val amountError: Boolean = false,
        val invalidAmountError: Boolean = false
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class LoadInitialData(val initialData: InitialData) : Intent()

        data class Message(val message: String) : Intent()

        // digit clicked from calculator
        data class OnDigitClicked(val digit: Int) : Intent()

        // operators clicked from calculator
        data class OnOperatorClicked(val operator: String) : Intent()

        // equal clicked from calculator
        object OnEqualClicked : Intent()

        // dot clicked from calculator
        object OnDotClicked : Intent()

        // long backpress from calculator
        object OnLongPressBackSpace : Intent()

        // backspace clicked from calculator
        object OnBackSpaceClicked : Intent()

        object ClearAmount : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        data class SetAmountDetails(
            val amount: Long,
            val amountCalculation: String,
            val calculatorOperatorsUsed: String
        ) : PartialState()

        object InvalidAmountError : PartialState()

        object ErrorState : PartialState()

        object ShowAmountError : PartialState()

        object HideAmountError : PartialState()

        object NoChange : PartialState()
    }

    interface Interactor

    data class InitialData(
        val initialAmount: Long,
        val initialAmountCalculation: String
    )

    interface Callback {
        fun callbackData(amountCalculation: String?, amount: Long, calculatorOperatorsUsed: String?)
        fun isInvalidAmount()
    }

    data class CallbackData(
        val amountCalculation: String?,
        val amount: Long,
        val calculatorOperatorsUsed: String?
    )
}
