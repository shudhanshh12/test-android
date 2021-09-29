package `in`.okcredit.shared.base

// UiState is the data required to completely render any given user interface
interface UiState {

    // Partial represents some change is the current state
    interface Partial<S : UiState>

    fun logUiState(currentState: UiState?) {
    }
}
