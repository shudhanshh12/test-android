package `in`.okcredit.shared.base

import `in`.okcredit.shared.BuildConfig
import android.content.Context
import com.jakewharton.rxrelay2.PublishRelay
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import tech.okcredit.android.base.extensions.className
import tech.okcredit.android.base.extensions.classType
import tech.okcredit.android.base.rxjava.SchedulerProvider
import timber.log.Timber
import javax.inject.Inject

abstract class BaseBottomDialogScreen<S : UiState>(val label: String) :
    ExpandedBottomSheetDialogFragment(),
    UserInterface<S> {

    @Inject
    lateinit var schedulerProvider: Lazy<SchedulerProvider>

    @Inject
    lateinit var analyticsHandler: AnalyticsHandler

    private lateinit var currentState: S

    protected fun getCurrentState(): S {
        return currentState
    }

    @Inject
    lateinit var viewModel: Lazy<MviViewModel<S>>

    protected val intentRelay by lazy { PublishRelay.create<UserIntent>() }

    private val subscriptions: CompositeDisposable by lazy { CompositeDisposable() }

    private var isFirstTime = true

    init {
        Timber.v("<<<<MVIScreen $className initialized")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()

        // observe state
        addSubscription(
            viewModel.get().state()
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
            viewModel.get().attachIntents(
                Observable.mergeArray(
                    userIntents(),
                    intentRelay
                ).doOnNext {
                    Timber.v("<<<<MVIScreen intent = ${it.classType}")
                }
                    .doOnNext { analyticsHandler.handleUserIntent(it) }
            )
        )

        if (isFirstTime) {
            isFirstTime = false
            val loadIntent = loadIntent()
            if (loadIntent != null) {
                addSubscription(
                    viewModel.get().attachLoadIntent(Observable.just(loadIntent))
                )
            }
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
