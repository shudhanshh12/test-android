package `in`.okcredit.payment.ui.juspay.ui.juspayWorkerFragment

import `in`.okcredit.payment.PaymentTestData
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.contract.ApiErrorType
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerContract
import `in`.okcredit.payment.ui.juspay.juspayWorkerFragment.JuspayWorkerViewModel
import `in`.okcredit.payment.usecases.GetJuspayInitiateAttributeFromServer
import `in`.okcredit.payment.usecases.GetJuspayProcessPayloadFromServer
import `in`.okcredit.payment.usecases.GetPaymentAttributeFromServerImpl
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.Unauthorized
import tech.okcredit.base.network.NetworkError

class JuspayWorkerViewModelTest {
    lateinit var testObserver: TestObserver<JuspayWorkerContract.State>
    private val initialState: JuspayWorkerContract.State = JuspayWorkerContract.State()
    private val getJuspayAttributesResponse: GetJuspayInitiateAttributeFromServer = mock()
    private val getJuspayProcessPayloadFromServer: GetJuspayProcessPayloadFromServer = mock()
    private val getPaymentAttributeFromServer: GetPaymentAttributeFromServerImpl = mock()
    private val paymentAnalyticsEvents: PaymentAnalyticsEvents = mock()

    private val viewModel = JuspayWorkerViewModel(
        initialState,
        { getJuspayAttributesResponse },
        { getJuspayProcessPayloadFromServer },
        { getPaymentAttributeFromServer },
        { paymentAnalyticsEvents }
    )

    @Before
    fun setup() {

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        testObserver = viewModel.state().test()
    }

    @Test
    fun `SetJuspayWorkerState sets state to JUSPAY_NO_STATE`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.just(PaymentTestData.PAYMENT_ATTRIBUTE))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_NO_STATE)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_NO_STATE)
        )
    }

    @Test
    fun `SetJuspayWorkerState sets state to JUSPAY_INITIATE_STARTED`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.just(PaymentTestData.PAYMENT_ATTRIBUTE))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_STARTED)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_STARTED)
        )
    }

    @Test
    fun `SetJuspayWorkerState sets state to JUSPAY_INITIATE_FINISHED`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.just(PaymentTestData.PAYMENT_ATTRIBUTE))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_FINISHED)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_FINISHED)
        )
    }

    @Test
    fun `SetJuspayWorkerState sets state to JUSPAY_PROCESS_STARTED`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.just(PaymentTestData.PAYMENT_ATTRIBUTE))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED)
        )
    }

    @Test
    fun `SetJuspayWorkerState sets state to JUSPAY_SDK_OPENED`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.just(PaymentTestData.PAYMENT_ATTRIBUTE))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_SDK_OPENED)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_SDK_OPENED)
        )
    }

    @Test
    fun `SetJuspayWorkerState sets state to JUSPAY_PROCESS_FINISHED`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.just(PaymentTestData.PAYMENT_ATTRIBUTE))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_FINISHED)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_FINISHED)
        )
    }

    @Test
    fun `SetJuspayWorkerState sets state to API_ERROR`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.just(PaymentTestData.PAYMENT_ATTRIBUTE))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.SetJuspayWorkerState(JuspayWorkerContract.JuspayWorkerState.API_ERROR)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.API_ERROR)
        )
    }

    @Test
    fun `fetchJuspayInitiateDataFromServer() returns success`() {
        val response = PaymentApiMessages.GetJuspayAttributesResponse("", "", "", listOf())
        whenever(getJuspayAttributesResponse.execute())
            .thenReturn(Observable.just(Result.Success(response)))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.Load))

        // expectations
        assertThat(
            testObserver.values()
                .first() == initialState.copy(juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_NO_STATE)
        )

        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                getJuspayInitiateResponse = response,
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_INITIATE_STARTED
            )
        )
    }

    @Test
    fun `fetchJuspayInitiateDataFromServer() returns network error`() {
        whenever(getJuspayAttributesResponse.execute())
            .thenReturn(Observable.just(Result.Failure(NetworkError("error", Throwable("network_error")))))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.Load))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.API_ERROR,
                apiErrorType = ApiErrorType.NETWORK
            )
        )
    }

    @Test
    fun `fetchJuspayInitiateDataFromServer() returns auth error`() {
        whenever(getJuspayAttributesResponse.execute())
            .thenReturn(Observable.just(Result.Failure(Unauthorized())))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.Load))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.API_ERROR,
                apiErrorType = ApiErrorType.AUTH
            )
        )
    }

    @Test
    fun `fetchJuspayInitiateDataFromServer() returns other error`() {
        whenever(getJuspayAttributesResponse.execute())
            .thenReturn(Observable.just(Result.Failure(Exception("some error"))))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.Load))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.API_ERROR,
                apiErrorType = ApiErrorType.OTHER
            )
        )
    }

    @Test
    fun `fetchJuspayProcessPayloadFromServer() returns success`() {
        val response = PaymentApiMessages.GetJuspayAttributesResponse("", "", "", listOf())
        viewModel.paymentAttributesResponse = PaymentTestData.PAYMENT_ATTRIBUTE
        viewModel.amount = 100L
        viewModel.linkId = ""
        whenever(
            getJuspayProcessPayloadFromServer.execute(
                paymentId = "",
                amount = viewModel.amount.toDouble().div(100),
                linkId = ""
            )
        )
            .thenReturn(Observable.just(Result.Success(response)))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.GetJuspayProcessPayload))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                getJuspayProcessResponse = response,
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.JUSPAY_PROCESS_STARTED
            )
        )
    }

    @Test
    fun `fetchJuspayProcessPayloadFromServer() returns network error`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.error(NetworkError("network_error", Throwable("network_error"))))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.GetPaymentAttribute("link_id", 1L)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.API_ERROR,
                apiErrorType = ApiErrorType.NETWORK
            )
        )
    }

    @Test
    fun `fetchJuspayProcessPayloadFromServer() returns auth error`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.error(Unauthorized()))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.GetPaymentAttribute("link_id", 1L)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.API_ERROR,
                apiErrorType = ApiErrorType.AUTH
            )
        )
    }

    @Test
    fun `fetchJuspayProcessPayloadFromServer() returns other error`() {
        whenever(getPaymentAttributeFromServer.execute("APP", "link_id"))
            .thenReturn(Single.error(Exception("some error")))

        // provide intent
        viewModel.attachIntents(Observable.just(JuspayWorkerContract.Intent.GetPaymentAttribute("link_id", 1L)))

        // expectations
        assertThat(
            testObserver.values()
                .last() == initialState.copy(
                juspayWorkerState = JuspayWorkerContract.JuspayWorkerState.API_ERROR,
                apiErrorType = ApiErrorType.OTHER
            )
        )
    }

    @After
    fun cleanup() {
        testObserver.dispose()
    }
}
