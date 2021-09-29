package `in`.okcredit.shared.base

import io.reactivex.Observable

// UserInterface takes care of capturing user intents and rendering state
interface UserInterface<in S : UiState> {
    fun userIntents(): Observable<UserIntent>

    fun render(state: S)

    fun loadIntent(): UserIntent? = null
}

interface UserInterfaceWithViewEvents<E : BaseViewEvent> {
    fun handleViewEvent(event: E)
}
