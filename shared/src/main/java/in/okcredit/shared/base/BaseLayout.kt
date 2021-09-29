package `in`.okcredit.shared.base

import `in`.okcredit.shared.BuildConfig
import `in`.okcredit.shared.utils.LayoutAutoDispose
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.jakewharton.rxrelay2.PublishRelay
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import dagger.internal.Preconditions
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import tech.okcredit.android.base.extensions.className
import tech.okcredit.android.base.rxjava.SchedulerProvider
import timber.log.Timber
import javax.inject.Inject

abstract class BaseLayout<S : UiState> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), UserInterface<S> {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var analyticsHandler: AnalyticsHandler

    private lateinit var currentState: S

    @Inject
    lateinit var viewModel: IBaseLayoutViewModel<S>

    val autoDisposable = LayoutAutoDispose()

    protected val intentRelay by lazy { PublishRelay.create<UserIntent>() }

    protected fun getCurrentState(): S {
        return currentState
    }

    private var isFirstTime = true

    private val subscriptions: CompositeDisposable by lazy { CompositeDisposable() }

    init {
        Timber.e("<<<<MVILayout $className initialized")
        injectLayout()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        autoDisposable.onAttached()

        // observe state
        addSubscription(
            viewModel.state()
                .observeOn(schedulerProvider.ui())
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
                Observable.mergeArray(
                    userIntents(),
                    intentRelay
                ).doOnNext { analyticsHandler.handleUserIntent(it) }
            )
        )

        if (isFirstTime) {
            isFirstTime = false
            addSubscription(
                viewModel.attachloadIntent(Observable.just(loadIntent()))
            )
        }

        Timber.e("<<<<firestore$className attached to ${viewModel.className}")
    }

    private fun injectLayout() {
        val hasAndroidInjector = findHasInjector()
        val androidInjector: AndroidInjector<Any> = hasAndroidInjector.androidInjector()
        Preconditions.checkNotNull(
            androidInjector, "%s.androidInjector() returned null", hasAndroidInjector.javaClass
        )

        androidInjector.inject(this)
    }

    private fun findHasInjector(): HasAndroidInjector {
        if (context is HasAndroidInjector) {
            return (context as HasAndroidInjector)
        }
        if (context.applicationContext is HasAndroidInjector) {
            return (context.applicationContext as HasAndroidInjector)
        }
        throw IllegalArgumentException(
            String.format(
                "No injector was found for %s",
                this.javaClass.canonicalName
            )
        )
    }

    override fun onDetachedFromWindow() {
        Timber.e("<<<<firestore$className detached from ${viewModel.className}")
        viewModel.dispose()
        subscriptions.clear()
        autoDisposable.onDetached()
        super.onDetachedFromWindow()
        Timber.v("<<<<MVIScreen $className detached from ${viewModel.className}")
    }

    private fun addSubscription(disposable: Disposable) {
        if (!disposable.isDisposed) {
            subscriptions.add(disposable)
        }
    }

    fun isStateInitialized() = ::currentState.isInitialized

    protected fun pushIntent(intent: UserIntent) = intentRelay.accept(intent)
}
