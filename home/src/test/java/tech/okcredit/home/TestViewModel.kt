package tech.okcredit.home

import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import java.util.concurrent.TimeUnit

// TODO: 28/01/21 Find a way to move this to shared/base module as java-test-fixtures is not working for android
//  libraries
abstract class TestViewModel<S : UiState, P : UiState.Partial<S>, E : BaseViewEvent> {

    private lateinit var testScheduler: TestScheduler
    private lateinit var stateObserver: TestObserver<S>
    private lateinit var viewEffectObserver: TestObserver<E>
    private lateinit var intentObserver: TestObserver<UserIntent>

    private lateinit var viewModel: BaseViewModel<S, P, E>

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        viewModel = createViewModel()

        viewEffectObserver = viewModel.viewEvent().test()
        stateObserver = viewModel.state().test()
        intentObserver = viewModel.intents().test()

        initDependencies()
    }

    /**
     * Get the last state in state observer
     */
    fun lastState(): S = stateObserver.values().last()

    /**
     * Get the last view event in view event observer
     */
    fun lastViewEvent(): E = viewEffectObserver.values().last()

    /**
     * Get the last intent attached to view model
     */
    fun lastIntent(): UserIntent = intentObserver.values().last()

    /**
     * Helper method to assert the last value of state
     */
    protected fun assertLastState(state: S) {
        val count = stateObserver.valueCount()
        stateObserver.assertValueAt(count - 1, state)
    }

    /**
     * Helper method to assert the last value of state
     */
    protected fun assertLastValue(predicate: (S) -> Boolean) {
        val count = stateObserver.valueCount()
        stateObserver.assertValueAt(count - 1) { predicate(it) }
    }

    /**
     * Helper method to assert the last value of view event observer
     */
    protected fun assertLastViewEvent(viewEvent: E) {
        Truth.assertThat(viewEffectObserver.values().last() == viewEvent).isTrue()
    }

    /**
     * Helper method to assert the last value of intent observer
     */
    protected inline fun <reified I : UserIntent> assertLastIntent() {
        Truth.assertThat(lastIntent() is I).isTrue()
    }

    /**
     * Helper method to add an intent to view model.
     * We are using [sample] in our state observer hence we are advancing time after pushing intent
     */
    protected fun pushIntent(intent: UserIntent, delay: Long = DEFAULT_DELAY_FOR_INTENT) {
        viewModel.attachIntents(Observable.just(intent))
        testScheduler.advanceTimeBy(delay, TimeUnit.MILLISECONDS)
    }

    @After
    fun cleanUp() {
        stateObserver.dispose()
        viewEffectObserver.dispose()
        tearDown()
    }

    /**
     * Create a view model which will be used to run all the test
     */
    abstract fun createViewModel(): BaseViewModel<S, P, E>

    /**
     * Initialise all the dependencies before all tests are run
     */
    open fun initDependencies() {
    }

    /**
     * Callback for cleaning up any resources when all the tests are finished
     */
    open fun tearDown() {
    }

    companion object {
        private const val DEFAULT_DELAY_FOR_INTENT = 35L
    }
}
