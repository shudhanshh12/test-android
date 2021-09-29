package `in`.okcredit.shared.base

import `in`.okcredit.shared.BuildConfig
import android.content.Context
import androidx.fragment.app.DialogFragment
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import tech.okcredit.android.base.extensions.className
import tech.okcredit.android.base.extensions.classType
import tech.okcredit.android.base.extensions.json
import tech.okcredit.android.base.rxjava.SchedulerProvider
import timber.log.Timber
import javax.inject.Inject

abstract class BaseDialogFragmentScreen<S : UiState> : DialogFragment(), UserInterface<S> {

    @Inject
    lateinit var schedulerProvider: Lazy<SchedulerProvider>

    @Inject
    lateinit var analyticsHandler: AnalyticsHandler

    private lateinit var currentState: S

    protected fun getCurrentState(): S {
        return currentState
    }

    @Inject
    lateinit var viewModel: MviViewModel<S>

    private val subscriptions: CompositeDisposable by lazy { CompositeDisposable() }

    init {
        Timber.v("<<<<MVIScreen $className initialized")
    }

    private var isFirstTime = true

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()

        // observe state
        addSubscription(
            viewModel.state()
                .observeOn(schedulerProvider.get().ui())
                .subscribe(
                    {
                        this.currentState = it
                        render(it)
                    },
                    {
                        if (BuildConfig.DEBUG) {
                            Timber.e(it.cause, "Exception inside render")
                            throw RuntimeException("UI Rendering Error", it)
                        }
                    }
                )
        )

        // attach intents
        addSubscription(
            viewModel.attachIntents(
                userIntents()
                    .doOnNext {
                        Timber.v("<<<<MVIScreen intent = ${it.classType} data = ${it.json()}")
                    }
                    .doOnNext { analyticsHandler.handleUserIntent(it) }
            )
        )

        if (isFirstTime) {
            isFirstTime = false
            addSubscription(
                viewModel.attachLoadIntent(Observable.just(loadIntent()))
            )
        }

        Timber.v("<<<<MVIScreen $className attached to ${viewModel.className}")
    }

    override fun onPause() {
        subscriptions.clear()
        super.onPause()

        Timber.v("<<<<MVIScreen $className detached from ${viewModel.className}")
    }

    private fun addSubscription(disposable: Disposable) {
        if (!disposable.isDisposed) {
            subscriptions.add(disposable)
        }
    }

    fun isStateInitialized() = ::currentState.isInitialized
}
