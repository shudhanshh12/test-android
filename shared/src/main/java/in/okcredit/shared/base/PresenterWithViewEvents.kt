package `in`.okcredit.shared.base

import io.reactivex.Observable

interface PresenterWithViewEvents<S : UiState, E : BaseViewEvent> : MviViewModel<S> {
    fun viewEvent(): Observable<E>
}
