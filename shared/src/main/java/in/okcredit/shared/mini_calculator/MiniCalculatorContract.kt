package `in`.okcredit.shared.mini_calculator

import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

interface MiniCalculatorContract {
    data class State(
        val isLoading: Boolean = true,
        val error: Boolean = false,
        val amountCalculation: String? = null,
        val amount: Long = 0L,
        val isIncorrectPassword: Boolean = false
    ) : UiState

    sealed class Intent : UserIntent {
        object Load : Intent()

        data class LoadInitialData(val initialData: IntialData) : Intent()

        // digit clicked from calculator
        data class OnDigitClicked(val digit: Int) : Intent()

        // dot clicked from calculator
        object OnDotClicked : Intent()

        // long backpress from calculator
        object OnLongPressBackSpace : Intent()

        // backspace clicked from calculator
        object OnBackSpaceClicked : Intent()
    }

    sealed class PartialState : UiState.Partial<State> {
        data class InitialData(val initialData: IntialData) : PartialState()
        data class SetAmountDetails(
            val amount: Long,
            val amountCalculation: String
        ) : PartialState()

        object ErrorState : PartialState()

        object ShowAmountError : PartialState()

        object HideAmountError : PartialState()

        object NoChange : PartialState()
    }

    interface Interactor

    interface Listener {
        fun gotoLogin()
    }

    data class IntialData(
        val amount: Long
    )

    interface Callback {
        fun miniCallbackData(amount: String, amount100Times: Long)
    }
}
