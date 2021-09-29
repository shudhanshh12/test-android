package `in`.okcredit.shared.base

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import tech.okcredit.android.base.error.check
import tech.okcredit.android.base.extensions.className
import tech.okcredit.base.network.utils.NetworkHelper
import timber.log.Timber

abstract class BaseLayoutViewModel<S : UiState, P : UiState.Partial<S>> protected constructor(
    // initial (or default) state of the state interface
    private val initialState: S,

    // scheduler on which state is reduced using partial states and pushed to stateRelay
    private val stateThread: Scheduler,

    // scheduler on which intents coming from the state interface is consumed and pushed to intentRelay
    private val intentThread: Scheduler
) : IBaseLayoutViewModel<S> {

    lateinit var newState: S

    // intent relay is used so that the `viewModel processing stream` doesn't break even when the state interface is detached
    private val intentRelay by lazy { PublishRelay.create<UserIntent>() }

    // state relay is used to preserve last known state, which is used by the state interface when it (re)attaches
    private val stateRelay: BehaviorRelay<S> by lazy { BehaviorRelay.createDefault(initialState) }

    // isStateSetup is used to enforce "only once" subscription of state
    private var isStateSetup: Boolean = false

    // subscriptions is used to save state subscription, and any other subscriptions added from the viewModel implementation
    private val subscriptions: CompositeDisposable by lazy { CompositeDisposable() }

    init {
        Timber.v("<<<<MVIScreen $className initialized")
    }

    final override fun state(): Observable<S> = stateRelay

    fun intents(): Observable<UserIntent> = intentRelay

    final override fun attachIntents(intents: Observable<UserIntent>): Disposable {
        setupState()
        return intents
            .observeOn(intentThread)
            .subscribe(intentRelay::accept)
    }

    final override fun attachloadIntent(intent: Observable<UserIntent>): Disposable {
        return intent
            .observeOn(intentThread)
            .subscribe(intentRelay::accept)
    }

    // handle takes care of all processing related to the given state interface (usually initiated because of an intent)
    // to produce partial states, which ultimately, is used to update state
    protected abstract fun handle(): Observable<out UiState.Partial<S>>

    // reduce creates updated state interface state using the previous state and a partial state
    protected abstract fun reduce(currentState: S, partialState: P): S

    protected fun addSubscription(disposable: Disposable) {
        if (!disposable.isDisposed) {
            subscriptions.add(disposable)
        }
    }

    protected inline fun <reified I : UserIntent> intent(): Observable<I> {
        val intentClass = I::class.java
        return intents()
            .filter { intentClass.isAssignableFrom(it.javaClass) }
            .cast(intentClass)
    }

    protected fun isInternetIssue(@NonNull throwable: Throwable): Boolean {
        return NetworkHelper.isNetworkError(throwable)
    }

    protected fun isAuthenticationIssue(@NonNull throwable: Throwable): Boolean {
        return throwable.check<tech.okcredit.android.auth.Unauthorized>()
    }

    private fun setupState() {
        synchronized(this) {
            if (isStateSetup) return

            // state handling
            addSubscription(
                this.handle()
                    .observeOn(stateThread)
                    .scan(initialState) { currentState, partialState ->
                        try {
                            newState = reduce(currentState, partialState as P)
                            newState
                        } catch (e: Exception) {
                            Timber.e("<<<<MVIScreen Error updating Presenter with error $e")
                            currentState
                        }
                    }
                    .distinctUntilChanged()
                    .subscribe {
                        // synchronized to prevent a ConcurrentModificationException
                        synchronized(this) {
                            stateRelay.accept(it)
                        }
                    }
            )

            isStateSetup = true
        }
    }

    protected fun addIntent(intent: UserIntent) = intentRelay.accept(intent)

    override fun dispose() {
        subscriptions.clear()
    }

    protected inline fun <reified T> genericCastOrNull(anything: Any): T? {
        return anything as? T
    }
}
