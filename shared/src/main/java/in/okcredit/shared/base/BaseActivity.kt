package `in`.okcredit.shared.base

import `in`.okcredit.communication_inappnotification.contract.InAppNotificationHandler
import `in`.okcredit.shared.BuildConfig
import `in`.okcredit.shared.performance.memory.MemoryDataTracer
import `in`.okcredit.shared.performance.memory.TrackMemoryData
import `in`.okcredit.shared.utils.AutoDisposable
import `in`.okcredit.shared.utils.addTo
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.rxrelay2.PublishRelay
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import tech.okcredit.android.base.BaseLanguageActivity
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.className
import tech.okcredit.android.base.extensions.requireView
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseActivity<S : UiState, E : BaseViewEvent, I : UserIntent>(val label: String) :
    BaseLanguageActivity(), UserInterface<S>, LifecycleObserver, UserInterfaceWithViewEvents<E> {

    @Inject
    lateinit var schedulerProvider: Lazy<SchedulerProvider>

    @Inject
    lateinit var analyticsHandler: Lazy<AnalyticsHandler>

    @Inject
    lateinit var firebasePerformance: Lazy<FirebasePerformance>

    @Inject
    lateinit var inAppNotificationHandler: Lazy<InAppNotificationHandler>

    private var inAppNotificationJob: Job? = null

    @Inject
    lateinit var firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>

    @Inject
    lateinit var trackMemoryData: Lazy<TrackMemoryData>

    private var disposable: Disposable? = null

    private lateinit var currentState: S

    val autoDisposable = AutoDisposable()

    protected fun getCurrentState(): S {
        return currentState
    }

    private val intentRelay by lazy { PublishRelay.create<UserIntent>() }

    @Inject
    lateinit var viewModel: Lazy<MviViewModel<S>>

    private val subscriptions: CompositeDisposable by lazy { CompositeDisposable() }

    private var isFirstTime = true

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
                        ExceptionUtils.logException(Exception(it))
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
                ).doOnNext { analyticsHandler.get().handleUserIntent(it) }
            )
        )

        if (isFirstTime) {
            isFirstTime = false
            addSubscription(
                viewModel.get().attachLoadIntent(Observable.just(loadIntent()))
            )
        }

        Timber.v("<<<<MVIScreen $className attached to ${viewModel.className}")

        initInAppNotificationHandler()
    }

    override fun onPause() {
        subscriptions.clear()
        super.onPause()
        Timber.v("<<<<MVIScreen $className detached from ${viewModel.className}")
        inAppNotificationJob?.cancel()
    }

    private fun addSubscription(disposable: Disposable) {
        if (!disposable.isDisposed) {
            subscriptions.add(disposable)
        }
    }

    fun isStateInitialized() = ::currentState.isInitialized

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(MemoryDataTracer(firebaseRemoteConfig, lifecycleScope, trackMemoryData, label))
        autoDisposable.bindTo(this.lifecycle)
        if (viewModel.get() is PresenterWithViewEvents<*, *>) {
            val viewModelWithViewEvent = viewModel.get() as PresenterWithViewEvents<*, *>
            disposable = viewModelWithViewEvent.viewEvent()
                .observeOn(schedulerProvider.get().ui())
                .subscribe(
                    {
                        val viewEvent = try {
                            it as E
                        } catch (e: Exception) {
                            e.printStackTrace()
                            RecordException
                                .recordException(IllegalArgumentException("Object is not an instance of expected ViewEvent"))
                            null
                        }
                        viewEvent?.let {
                            handleViewEvent(viewEvent)
                        }
                    },
                    {
                        it.printStackTrace()
                        RecordException.recordException(it)
                    }
                )
        }
    }

    protected fun pushIntent(intent: I) = intentRelay.accept(intent)

    protected fun pushIntentWithDelay(intent: I, delay: Long = DEFAULT_DELAY_FOR_PUSH_INTENT) {
        Completable
            .timer(delay, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                pushIntent(intent)
            }.addTo(autoDisposable)
    }

    private fun initInAppNotificationHandler() {
        inAppNotificationJob =
            lifecycleScope.launchWhenResumed {
                // label is the screen name passed by the child class
                inAppNotificationHandler.get()
                    .execute(label, WeakReference(this@BaseActivity), WeakReference(requireView()))
            }
    }

    companion object {
        const val DEFAULT_DELAY_FOR_PUSH_INTENT = 500L
    }
}
