package `in`.okcredit.shared.base

import `in`.okcredit.analytics.ANRDebugger
import `in`.okcredit.communication_inappnotification.contract.InAppNotificationHandler
import `in`.okcredit.shared.R
import `in`.okcredit.shared.performance.PerformanceTracker
import `in`.okcredit.shared.performance.frame_rate.FragmentFrameRateTracer
import `in`.okcredit.shared.performance.memory.MemoryDataTracer
import `in`.okcredit.shared.performance.memory.TrackMemoryData
import `in`.okcredit.shared.utils.AutoDisposable
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.camera.extensions.BuildConfig
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.rxrelay2.PublishRelay
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.className
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

@Deprecated(message = "Use BaseScreenWithViewEvents instead", replaceWith = ReplaceWith("BaseScreenWithViewEvents"))
abstract class BaseScreen<S : UiState>(val label: String, @LayoutRes contentLayoutId: Int = 0) :
    Fragment(contentLayoutId),
    UserInterface<S>,
    LifecycleObserver {

    @Inject
    lateinit var schedulerProvider: Lazy<SchedulerProvider>

    @Inject
    lateinit var analyticsHandler: Lazy<AnalyticsHandler>

    @Inject
    lateinit var firebasePerformance: Lazy<FirebasePerformance>

    @Inject
    lateinit var performanceTracker: Lazy<PerformanceTracker>

    @Inject
    lateinit var inAppNotificationHandler: Lazy<InAppNotificationHandler>

    @Inject
    lateinit var firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>

    @Inject
    lateinit var anrDebugger: Lazy<ANRDebugger>

    @Inject
    lateinit var trackMemoryData: Lazy<TrackMemoryData>

    private lateinit var currentState: S

    protected val autoDisposable = AutoDisposable()

    private var inAppNotificationJob: Job? = null

    protected fun getCurrentState(): S {
        return currentState
    }

    // TODO Generics to be used
    protected val intentRelay by lazy { PublishRelay.create<UserIntent>() }

    @Inject
    lateinit var viewModel: Lazy<MviViewModel<S>>

    private val subscriptions: CompositeDisposable by lazy { CompositeDisposable() }

    private lateinit var tracerViewCreate: Trace

    private var isFirstTime = true

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        tracerViewCreate = firebasePerformance.get().newTrace("Create${label.replace(" ", "_")}")
        tracerViewCreate.start()
        autoDisposable.bindTo(this.lifecycle)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        anrDebugger.get().setCurrentScreen(label)
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
            val loadIntent = loadIntent()
            if (loadIntent != null) {
                addSubscription(
                    viewModel.get().attachLoadIntent(Observable.just(loadIntent))
                )
            }
        }

        activity?.findViewById<Toolbar>(R.id.toolbar)?.let {
            it.setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }

        initInAppNotificationHandler()
    }

    override fun onPause() {
        subscriptions.clear()
        super.onPause()

        Timber.v("<<<<MVIScreen $className detached from ${viewModel.className}")

        inAppNotificationJob?.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tracerViewCreate.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sampling = firebaseRemoteConfig.get().getLong(FRAME_RATE_TRACKING_SAMPLING_KEY).toInt()
        if ((0..100).random() < sampling) {
            lifecycle.addObserver(FragmentFrameRateTracer(requireActivity(), performanceTracker.get(), label))
        }

        lifecycle.addObserver(MemoryDataTracer(firebaseRemoteConfig, lifecycleScope, trackMemoryData, label))
    }

    protected fun addSubscription(disposable: Disposable) {
        if (!disposable.isDisposed) {
            subscriptions.add(disposable)
        }
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    fun isStateInitialized() = ::currentState.isInitialized

    fun isPermissionNotGranted(permission: String) = !isPermissionGranted(permission)

    fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun isAllRequiredPermissionsGranted(requiredPermissions: Array<String>): Boolean {
        return requiredPermissions.none { isPermissionNotGranted(it) }
    }

    fun askPermissions(requiredPermissions: Array<String>, requestCode: Int) {
        requestPermissions(
            requiredPermissions.filter { isPermissionNotGranted(it) }.toTypedArray(),
            requestCode
        )
    }

    private fun initInAppNotificationHandler() {
        if (view != null) { // fragment should not be headless
            inAppNotificationJob =
                viewLifecycleOwner.lifecycleScope.launch(inAppNotificationHandler.get().getExceptionHandler()) {
                    inAppNotificationHandler.get()
                        .execute(label, WeakReference(requireActivity()), WeakReference(requireView()))
                }
        }
    }

    companion object {
        const val FRAME_RATE_TRACKING_SAMPLING_KEY = "frame_rate_tracking_sampling"
        const val DEVICE_MEMORY_TRACKING_SAMPLING = "memory_tracking_sampling"
    }
}
