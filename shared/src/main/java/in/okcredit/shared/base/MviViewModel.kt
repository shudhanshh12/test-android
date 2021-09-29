package `in`.okcredit.shared.base

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

// Presenter orchestrates a given user interface by consuming user intents and updating ui state
interface MviViewModel<S : UiState> {
    fun state(): Observable<S>

    fun attachIntents(intents: Observable<UserIntent>): Disposable

    fun attachLoadIntent(intent: Observable<UserIntent>): Disposable
}
