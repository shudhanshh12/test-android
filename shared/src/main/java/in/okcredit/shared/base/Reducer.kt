package `in`.okcredit.shared.base

/**
 * Interface which can be used move out reducer logic from ViewModel if they are become huge.
 */
interface Reducer<State : UiState, P : UiState.Partial<State>> {
    fun reduce(current: State, partial: P): State
}
