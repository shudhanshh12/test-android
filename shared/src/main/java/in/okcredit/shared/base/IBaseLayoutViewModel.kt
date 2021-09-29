package `in`.okcredit.shared.base

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface IBaseLayoutViewModel<S : UiState> {
    fun state(): Observable<S>

    fun attachIntents(intents: Observable<UserIntent>): Disposable
    fun attachloadIntent(intent: Observable<UserIntent>): Disposable
    fun setNavigation(baseLayout: BaseLayout<S>)
    fun dispose()
}
