package `in`.okcredit.payment.ui.payment_destination

import `in`.okcredit.payment.PaymentTestData
import `in`.okcredit.payment.R
import `in`.okcredit.payment.usecases.SetPaymentDestinationToServer
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.base.network.NetworkError
import java.util.concurrent.TimeUnit

class PaymentDestinationViewModelTest {

    lateinit var testObserverViewEvent: TestObserver<PaymentDestinationContract.ViewEvents>
    lateinit var testObserver: TestObserver<PaymentDestinationContract.State>

    private lateinit var testScheduler: TestScheduler
    private val initialState = PaymentDestinationContract.State()
    private val adoptionSource: String = "TEST"
    private val setPaymentDestination: SetPaymentDestinationToServer = mock()
    private val context: Context = mock()

    private lateinit var viewModel: PaymentDestinationViewModel

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()
        every { Schedulers.single() } returns Schedulers.trampoline()

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        viewModel = PaymentDestinationViewModel(
            initialState,
            adoptionSource,
            { setPaymentDestination },
            { context }
        )

        testObserver = viewModel.state().test()

        testObserverViewEvent = viewModel.viewEvent().test()

        whenever(context.getString(R.string.payment_no_internet_connection)).thenReturn("No internet connection")
        whenever(context.getString(R.string.payment_error_not_able_to_Add_details)).thenReturn("Unable to add your details,Please try again!")
    }

    @Test
    fun `setAdoptionModeObservable() to bank`() {

        viewModel.attachIntents(Observable.just(PaymentDestinationContract.Intent.SetAdoptionMode("bank")))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        // expectations
        assertThat(testObserver.values().first() == initialState).isTrue()
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                adoptionMode = "bank",
                showUi = PaymentDestinationContract.UiScreenType.BANK
            )
        ).isTrue()
    }

    @Test
    fun `setAdoptionModeObservable() to upi`() {

        viewModel.attachIntents(Observable.just(PaymentDestinationContract.Intent.SetAdoptionMode("upi")))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        // expectations
        assertThat(testObserver.values().first() == initialState).isTrue()
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                adoptionMode = "upi",
                showUi = PaymentDestinationContract.UiScreenType.UPI
            )
        ).isTrue()
    }

    @Test
    fun `EnteredUPI() success`() {

        viewModel.attachIntents(Observable.just(PaymentDestinationContract.Intent.EnteredUPI("upi@123")))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        // expectations
        assertThat(testObserver.values().first() == initialState).isTrue()
        assertThat(
            testObserver.values()
                .last() == initialState.copy(enteredUpi = "upi@123")
        ).isTrue()
    }

    @Test
    fun `EnteredIfsc() success`() {

        viewModel.attachIntents(Observable.just(PaymentDestinationContract.Intent.EnteredIfsc("HDFC00012344")))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        // expectations
        assertThat(testObserver.values().first() == initialState).isTrue()
        assertThat(
            testObserver.values()
                .last() == initialState.copy(enteredIfsc = "HDFC00012344")
        ).isTrue()
    }

    @Test
    fun `EnteredAccountNumber() success`() {

        viewModel.attachIntents(Observable.just(PaymentDestinationContract.Intent.EnteredAccountNumber("123400012344")))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        // expectations
        assertThat(testObserver.values().first() == initialState).isTrue()
        assertThat(
            testObserver.values()
                .last() == initialState.copy(enteredAccountNumber = "123400012344")
        ).isTrue()
    }

    @Test
    fun `SetDestinationToServer() bank`() {

        whenever(
            setPaymentDestination.execute(
                adoptionSource = adoptionSource,
                paymentType = "bank",
                paymentAddress = "123400012344@HDFC000123"
            )
        ).thenReturn(
            Observable.just(
                Result.Success(
                    PaymentTestData.getPaymentDestinationResponse(
                        "bank",
                        "123400012344@HDFC000123",
                        "Poonam Parth"
                    )
                )
            )
        )

        viewModel.attachIntents(
            Observable.just(
                PaymentDestinationContract.Intent.SetDestinationToServer(
                    paymentType = "bank",
                    paymentAddress = "123400012344@HDFC000123"
                )
            )
        )
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        assertThat(testObserver.values().first() == initialState).isTrue()
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                accountHolderName = "Poonam Parth",
                showUi = PaymentDestinationContract.UiScreenType.SUCCESS
            )
        ).isTrue()
        assertThat(
            testObserverViewEvent.values()
                .last() == PaymentDestinationContract.ViewEvents.OnAccountAddedSuccessfully
        ).isTrue()
    }

    @Test
    fun `SetDestinationToServer() upi`() {
        whenever(
            setPaymentDestination.execute(
                adoptionSource = adoptionSource,
                paymentType = "upi",
                paymentAddress = "12340001234@ybl"
            )
        ).thenReturn(
            Observable.just(
                Result.Success(
                    PaymentTestData.getPaymentDestinationResponse(
                        "upi",
                        "12340001234@ybl",
                        "Poonam Parth"
                    )
                )
            )
        )

        viewModel.attachIntents(
            Observable.just(
                PaymentDestinationContract.Intent.SetDestinationToServer(
                    paymentType = "upi",
                    paymentAddress = "12340001234@ybl"
                )
            )
        )
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        // expectations
        assertThat(testObserver.values().first() == initialState).isTrue()
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                accountHolderName = "Poonam Parth",
                showUi = PaymentDestinationContract.UiScreenType.SUCCESS
            )
        ).isTrue()
        assertThat(
            testObserverViewEvent.values()
                .last() == PaymentDestinationContract.ViewEvents.OnAccountAddedSuccessfully
        ).isTrue()
    }

    @Test
    fun `SetDestinationToServer() returns network error`() {

        whenever(
            setPaymentDestination.execute(
                adoptionSource = adoptionSource,
                paymentType = "bank",
                paymentAddress = "123400012344@HDFC000123"
            )
        ).thenReturn(
            Observable.just(
                Result.Failure(
                    NetworkError(
                        cause = Throwable(
                            "network connection"
                        )
                    )
                )
            )
        )

        viewModel.attachIntents(
            Observable.just(
                PaymentDestinationContract.Intent.SetDestinationToServer(
                    paymentType = "bank",
                    paymentAddress = "123400012344@HDFC000123"
                )
            )
        )
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // expectations
        assertThat(testObserver.values().first() == initialState).isTrue()

        assertThat(
            testObserverViewEvent.values()
                .last() == PaymentDestinationContract.ViewEvents.ShowError(context.getString(R.string.payment_no_internet_connection))
        ).isTrue()
    }

    @Test
    fun `SetDestinationToServer() paymentType = "upi" returns other error`() {
        whenever(
            setPaymentDestination.execute(
                adoptionSource = adoptionSource,
                paymentType = "upi",
                paymentAddress = "123400012344@HDFC000123"
            )
        ).thenReturn(Observable.just(Result.Failure(Throwable("Something went wrong"))))

        viewModel.attachIntents(
            Observable.just(
                PaymentDestinationContract.Intent.SetDestinationToServer(
                    paymentType = "upi",
                    paymentAddress = "123400012344@HDFC000123"
                )
            )
        )
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        // expectations
        assertThat(testObserver.values().first() == initialState).isTrue()
        assertThat(
            testObserverViewEvent.values()
                .last() == PaymentDestinationContract.ViewEvents.ShowError("Something went wrong")
        ).isTrue()

        assertThat(
            testObserver.values().last() == initialState.copy(
                adoptionMode = "upi",
                showUi = PaymentDestinationContract.UiScreenType.UPI
            )
        ).isTrue()
    }

    @After
    fun cleanup() {
        testObserver.dispose()
        testObserverViewEvent.dispose()
    }
}
