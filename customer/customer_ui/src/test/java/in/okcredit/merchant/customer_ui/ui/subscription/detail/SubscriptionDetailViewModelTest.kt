package `in`.okcredit.merchant.customer_ui.ui.subscription.detail

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionStatus
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.DeleteSubscription
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.GetSubscription
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import tech.okcredit.base.network.NetworkError
import java.io.IOException
import java.util.concurrent.TimeUnit

class SubscriptionDetailViewModelTest {

    private lateinit var viewModel: SubscriptionDetailViewModel

    private val deleteSubscription: DeleteSubscription = mock()
    private val getSubscription: GetSubscription = mock()
    private val getCustomer: GetCustomer = mock()

    private lateinit var testScheduler: TestScheduler
    private lateinit var testObserver: TestObserver<SubscriptionDetailContract.State>
    private lateinit var viewEffectObserver: TestObserver<SubscriptionDetailContract.ViewEvent>

    private fun createPresenter(
        initialState: SubscriptionDetailContract.State,
        subscriptionId: String = TestData.DAILY_SUBSCRIPTION.id,
        subscription: Subscription? = TestData.DAILY_SUBSCRIPTION
    ) = SubscriptionDetailViewModel(
        initialState = { initialState },
        customerId = TestData.CUSTOMER.id,
        subscriptionId = subscriptionId,
        subscription = subscription,
        getSubscription = { getSubscription },
        deleteSubscription = { deleteSubscription },
        getCustomer = { getCustomer }
    )

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        val initialState = SubscriptionDetailContract.State()
        viewModel = createPresenter(initialState)

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler
        testObserver = viewModel.state().test()
        viewEffectObserver = viewModel.viewEvent().test()

        whenever(getCustomer.execute(TestData.CUSTOMER.id)).thenReturn(Observable.just(TestData.CUSTOMER))
    }

    @Test
    fun `load intent if subscription present in presenter param`() {
        val presenter = createPresenter(
            initialState = SubscriptionDetailContract.State(),
            subscriptionId = TestData.DAILY_SUBSCRIPTION.id,
            subscription = TestData.DAILY_SUBSCRIPTION
        )

        val testObserver = presenter.state().test()

        val initialState = testObserver.values().last()

        // push load  intent
        presenter.attachIntents(Observable.just(SubscriptionDetailContract.Intent.Load))
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        Assert.assertTrue(
            testObserver.values().last() == initialState.copy(
                customer = TestData.CUSTOMER,
                amount = TestData.DAILY_SUBSCRIPTION.amount,
                name = TestData.DAILY_SUBSCRIPTION.name,
                frequency = SubscriptionFrequency.getFrequency(TestData.DAILY_SUBSCRIPTION.frequency),
                startDate = TestData.DAILY_SUBSCRIPTION.startDate,
                nexDate = TestData.DAILY_SUBSCRIPTION.nextSchedule,
                status = SubscriptionStatus.getStatus(TestData.DAILY_SUBSCRIPTION.status)
            )
        )
    }

    @Test
    fun `load intent if subscription is not present in presenter param`() {
        whenever(
            getSubscription.execute(
                subscriptionId = viewModel.subscription!!.id,
            )
        ).thenReturn(
            Observable.just(
                Result.Success(TestData.DAILY_SUBSCRIPTION)
            )
        )
        val presenter = createPresenter(
            initialState = SubscriptionDetailContract.State(),
            subscriptionId = TestData.DAILY_SUBSCRIPTION.id,
            subscription = null
        )

        val testObserver = presenter.state().test()

        val initialState = testObserver.values().last()

        // push load  intent
        presenter.attachIntents(Observable.just(SubscriptionDetailContract.Intent.Load))

        verify(getSubscription).execute(TestData.DAILY_SUBSCRIPTION.id)

        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        Assert.assertTrue(
            testObserver.values().last() == initialState.copy(
                customer = TestData.CUSTOMER,
                amount = TestData.DAILY_SUBSCRIPTION.amount,
                name = TestData.DAILY_SUBSCRIPTION.name,
                frequency = SubscriptionFrequency.getFrequency(TestData.DAILY_SUBSCRIPTION.frequency),
                startDate = TestData.DAILY_SUBSCRIPTION.startDate,
                nexDate = TestData.DAILY_SUBSCRIPTION.nextSchedule,
                status = SubscriptionStatus.getStatus(TestData.DAILY_SUBSCRIPTION.status)
            )
        )
    }

    @Test
    fun `network error emits internet issue view event`() {
        whenever(
            getSubscription.execute(
                subscriptionId = viewModel.subscription!!.id,
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
        val presenter = createPresenter(
            initialState = SubscriptionDetailContract.State(),
            subscriptionId = TestData.DAILY_SUBSCRIPTION.id,
            subscription = null
        )

        val viewObserver = presenter.viewEvent().test()
        val count = viewObserver.valueCount()
        // push load  intent
        presenter.attachIntents(Observable.just(SubscriptionDetailContract.Intent.Load))

        verify(getSubscription).execute(TestData.DAILY_SUBSCRIPTION.id)
        viewObserver.awaitCount(count + 1)
        Assert.assertTrue(
            (viewObserver.values().last() is SubscriptionDetailContract.ViewEvent.ShowError)
        )
    }

    @Test
    fun `other error emits generic error`() {
        whenever(
            getSubscription.execute(
                subscriptionId = viewModel.subscription!!.id,
            )
        ).thenReturn(
            Observable.just(
                Result.Failure(
                    RuntimeException("")
                )
            )
        )
        val presenter = createPresenter(
            initialState = SubscriptionDetailContract.State(),
            subscriptionId = TestData.DAILY_SUBSCRIPTION.id,
            subscription = null
        )

        val viewObserver = presenter.viewEvent().test()
        val count = viewObserver.valueCount()

        // push load  intent
        presenter.attachIntents(Observable.just(SubscriptionDetailContract.Intent.Load))

        verify(getSubscription).execute(TestData.DAILY_SUBSCRIPTION.id)

        viewObserver.awaitCount(count + 1)
        Assert.assertTrue(
            (viewObserver.values().last() is SubscriptionDetailContract.ViewEvent.ShowError)
        )
    }

    @Test
    fun `delete intent fires delete confirm`() {
        val currentState = testObserver.values().last()
        // push add note clicked intent
        pushIntent(SubscriptionDetailContract.Intent.DeleteSubscription)
        // check for correct view event to be fired
        Assert.assertTrue(
            (viewEffectObserver.values().last() is SubscriptionDetailContract.ViewEvent.ShowDeleteConfirm)
        )
        // check that no change in current state
        Assert.assertTrue(testObserver.values().last() == currentState)
    }

    @Test
    fun `delete confirm executes delete subscription`() {
        whenever(
            deleteSubscription.execute(
                viewModel.subscription!!.copy(status = 2)
            )
        ).thenReturn(
            Observable.just(
                Result.Success(Unit)
            )
        )

        pushIntent(SubscriptionDetailContract.Intent.DeleteConfirmed)

        assert(viewModel.subscription != null)

        verify(deleteSubscription).execute(
            viewModel.subscription!!.copy(status = 2)
        )
    }

    @Test
    fun `delete subscription failure`() {
        whenever(
            deleteSubscription.execute(
                viewModel.subscription!!.copy(status = 2)
            )
        ).thenReturn(
            Observable.just(Result.Failure(IOException("network_error")))
        )

        pushIntent(SubscriptionDetailContract.Intent.DeleteConfirmed)

        // check for correct view event to be fired
        Assert.assertTrue(
            (viewEffectObserver.values().last() is SubscriptionDetailContract.ViewEvent.ShowError)
        )
    }

    @After
    fun tearDown() {
        testObserver.dispose()
        viewEffectObserver.dispose()
    }

    private fun pushIntent(intent: UserIntent) = viewModel.attachIntents(Observable.just(intent))
}
