package `in`.okcredit.shared.base

import `in`.okcredit.shared.utils.addTo
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.crashlytics.RecordException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseFragment<S : UiState, E : BaseViewEvent, I : UserIntent>(
    label: String,
    @LayoutRes contentLayoutId: Int = 0
) : BaseScreen<S>(label, contentLayoutId), UserInterfaceWithViewEvents<E> {

    // TODO: remove this dependency after upgrading dagger version >= 2.27
    @Inject
    lateinit var dispatcherProvider: Lazy<DispatcherProvider>

    private var disposable: Disposable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.get() is PresenterWithViewEvents<*, *>) {
            val viewModelWithViewEvent = viewModel.get() as PresenterWithViewEvents<*, *>
            disposable = viewModelWithViewEvent.viewEvent()
                .observeOn(schedulerProvider.get().ui())
                .subscribe(
                    {
                        val viewEvent = try {
                            it as E
                        } catch (e: Exception) {
                            RecordException
                                .recordException(IllegalArgumentException("Object is not an instance of expected ViewEvent"))
                            null
                        }
                        viewEvent?.let {
                            handleViewEvent(viewEvent)
                        }
                    },
                    {
                        RecordException.recordException(it)
                    }
                )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable?.dispose()
    }

    protected fun pushIntent(intent: I) = intentRelay.accept(intent)

    protected fun loadIntents(vararg intents: I) = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        intents.forEach {
            intentRelay.accept(it)
        }
    }

    protected fun pushIntentWithDelay(intent: I, delay: Long = DEFAULT_DELAY_FOR_PUSH_INTENT) {
        Completable
            .timer(delay, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                pushIntent(intent)
            }.addTo(autoDisposable)
    }

    companion object {
        const val DEFAULT_DELAY_FOR_PUSH_INTENT = 500L
    }
}
