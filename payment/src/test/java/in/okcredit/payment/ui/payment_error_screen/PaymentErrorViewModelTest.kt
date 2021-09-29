package `in`.okcredit.payment.ui.payment_error_screen

import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class PaymentErrorViewModelTest {

    lateinit var testObserver: TestObserver<PaymentErrorContract.State>
    private val testObserverViewEvent = TestObserver<PaymentErrorContract.ViewEvents>()

    private val initialState = PaymentErrorContract.State()
    private var errorType: String = PaymentErrorType.OTHER.value
    private val supplierId: String = "supplier_id"
    private val errorMessage: String = "error_message"
    private val supplierAnalyticsEvents: PaymentAnalyticsEvents = mock()
    private lateinit var testScheduler: TestScheduler
    private lateinit var viewModel: PaymentErrorViewModel

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()
        every { Schedulers.single() } returns Schedulers.trampoline()

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        viewModel = PaymentErrorViewModel(
            initialState,
            errorType,
            supplierId,
            errorMessage
        ) { supplierAnalyticsEvents }

        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(testObserverViewEvent)
    }

    @Test
    fun `loadObservable when error type other`() {
        viewModel.attachIntents(Observable.just(PaymentErrorContract.Intent.Load))
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assert(testObserver.values().last().errorType == PaymentErrorType.OTHER)
    }

    @Test
    fun `loadObservable when error type Network`() {
        val viewModel = PaymentErrorViewModel(
            initialState,
            PaymentErrorType.NETWORK.value,
            supplierId,
            errorMessage
        ) { supplierAnalyticsEvents }

        viewModel.attachIntents(Observable.just(PaymentErrorContract.Intent.Load))
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        viewModel.state().test().assertValue { it.errorType == PaymentErrorType.NETWORK }
    }

    @Test
    fun `retryObservable when error type Other`() {

        viewModel.attachIntents(Observable.just(PaymentErrorContract.Intent.OnRetry))
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        Truth.assertThat(
            testObserver.values()
                .last() == initialState
        ).isTrue()

        testObserverViewEvent.assertValue(PaymentErrorContract.ViewEvents.OnRetry)
    }

    @After
    fun cleanUp() {
        testObserver.dispose()
        testObserverViewEvent.dispose()
    }
}
