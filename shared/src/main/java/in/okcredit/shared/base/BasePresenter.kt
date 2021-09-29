package `in`.okcredit.shared.base

import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import tech.okcredit.android.base.error.check
import tech.okcredit.android.base.extensions.className
import tech.okcredit.base.exceptions.ExceptionUtils
import tech.okcredit.base.network.utils.NetworkHelper
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Deprecated(
    message = "Use BaseViewModel instead",
    replaceWith = ReplaceWith("BaseViewModel")
)
abstract class BasePresenter<S : UiState, P : UiState.Partial<S>> protected constructor(
    // initial (or default) state of the state interface
    private val initialState: S,

    // scheduler on which state is reduced using partial states and pushed to stateRelay
    private val stateThread: Scheduler = Schedulers.newThread(),

    // scheduler on which intents coming from the state interface is consumed and pushed to intentRelay
    private val intentThread: Scheduler = Schedulers.newThread()
) : MviViewModel<S>, ViewModel() {

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

    protected fun getCurrentState() = requireNotNull(stateRelay.value)

    fun intents(): Observable<UserIntent> = intentRelay

    final override fun attachIntents(intents: Observable<UserIntent>): Disposable {
        setupState()
        return intents
            .observeOn(intentThread)
            .subscribe {
                Timber.d("<<<<Intent: $it")
                intentRelay.accept(it)
            }
    }

    final override fun attachLoadIntent(intent: Observable<UserIntent>): Disposable {
        return intent
            .observeOn(intentThread)
            .doOnNext {
                Timber.d("<<<<Intent: $it")
            }
            .subscribe {
                intentRelay.accept(it)
            }
    }

    override fun onCleared() {
        subscriptions.clear()
        Timber.v("<<<<MVIScreen $className destroyed")
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
                            val newState = reduce(currentState, partialState as P)
                            newState
                        } catch (e: Exception) {
                            ExceptionUtils.logException(e)
                            Timber.e("<<<<MVIPresenter Reduce Error $e")
                            currentState
                        }
                    }
                    .distinctUntilChanged()
                    .sample(32, TimeUnit.MILLISECONDS)
                    .subscribe(
                        {
                            synchronized(this) {
                                stateRelay.accept(it)
                            }
                        },
                        {
                            var error = ""
                            for (i in it.stackTrace) {
                                error += "filename  ${i.fileName}  classname ${i.className} lineName ${i.lineNumber} methodName ${i.methodName}"
                                Timber.e("filename  ${i.fileName}  classname ${i.className} lineName ${i.lineNumber} methodName ${i.methodName}")
                            }
                            Timber.e("<<<<MVIPresenter Error $it")
                            Timber.e("<<<<MVIPresenter Error stacktrace $error")
                            ExceptionUtils.logException(Exception(it))
                        }
                    )
            )

            isStateSetup = true
        }
    }

    protected fun pushIntent(intent: UserIntent) = intentRelay.accept(intent)
}
