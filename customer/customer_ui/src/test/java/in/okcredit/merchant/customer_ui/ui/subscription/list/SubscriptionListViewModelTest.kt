package `in`.okcredit.merchant.customer_ui.ui.subscription.list

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy.SubscriptionItem
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.ListSubscriptions
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import tech.okcredit.base.network.NetworkError
import java.util.concurrent.TimeUnit

class SubscriptionListViewModelTest {
    private lateinit var viewModel: SubscriptionListViewModel

    private val listSubscriptions: ListSubscriptions = mock()
    private val getCustomer: GetCustomer = mock()

    private lateinit var testScheduler: TestScheduler
    private lateinit var testObserver: TestObserver<SubscriptionListContract.State>
    private lateinit var viewEffectObserver: TestObserver<SubscriptionListContract.ViewEvent>

    private fun createPresenter(
        initialState: SubscriptionListContract.State,
    ) = SubscriptionListViewModel(
        initialState = { initialState },
        customerId = TestData.CUSTOMER.id,
        listSubscriptions = { listSubscriptions },
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

        val initialState = SubscriptionListContract.State()
        viewModel = createPresenter(initialState)
        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler
        testObserver = viewModel.state().test()
        viewEffectObserver = viewModel.viewEvent().test()

        whenever(getCustomer.execute(TestData.CUSTOMER.id)).thenReturn(Observable.just(TestData.CUSTOMER))
    }

    @Test
    fun `load intent gets list of subscription`() {
        whenever(
            listSubscriptions.execute(customerId = viewModel.customerId)
        ).thenReturn(
            Observable.just(
                Result.Success(
                    listOf(
                        TestData.DAILY_SUBSCRIPTION,
                    )
                )
            )
        )

        val presenter = createPresenter(SubscriptionListContract.State())
        val testObserver = presenter.state().test()
        val initialState = testObserver.values().last()
        // push load  intent
        presenter.attachIntents(Observable.just(SubscriptionListContract.Intent.Load))

        verify(listSubscriptions).execute(TestData.CUSTOMER.id)
        verify(getCustomer).execute(TestData.CUSTOMER.id)

        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        Assert.assertTrue(
            testObserver.values().last() == initialState.copy(
                customerName = TestData.CUSTOMER.description,
                list = listOf(
                    SubscriptionItem(
                        id = TestData.DAILY_SUBSCRIPTION.id,
                        name = TestData.DAILY_SUBSCRIPTION.name,
                        frequency = SubscriptionFrequency.DAILY,
                        daysInWeek = TestData.DAILY_SUBSCRIPTION.week?.map { day -> DayOfWeek.getDay(day) },
                        startDate = TestData.DAILY_SUBSCRIPTION.startDate
                    )
                )
            )
        )
    }

    @Test
    fun `item clicked fires go to subscription detail`() {
        val currentState = testObserver.values().last()
        // push add note clicked intent
        pushIntent(SubscriptionListContract.Intent.ItemClicked(TestData.DAILY_SUBSCRIPTION.id))
        // check for correct view event to be fired
        Assert.assertTrue(
            (viewEffectObserver.values().last() is SubscriptionListContract.ViewEvent.GoToSubscriptionDetail)
        )
        // check that no change in current state
        Assert.assertTrue(testObserver.values().last() == currentState)
    }

    @Test
    fun `add subscription fires go to add subscription`() {
        val currentState = testObserver.values().last()
        // push add note clicked intent
        pushIntent(SubscriptionListContract.Intent.AddSubscriptionClicked)
        // check for correct view event to be fired
        Assert.assertTrue(
            (viewEffectObserver.values().last() is SubscriptionListContract.ViewEvent.GoToAddSubscription)
        )
        // check that no change in current state
        Assert.assertTrue(testObserver.values().last() == currentState)
    }

    @Test
    fun `refresh data calls list subscriptions again`() {
        // push add note clicked intent
        pushIntent(SubscriptionListContract.Intent.RefreshData)

        // check for correct view event to be fired
        verify(listSubscriptions).execute(TestData.CUSTOMER.id)
    }

    @Test
    fun `network error emits internet issue view event`() {
        whenever(
            listSubscriptions.execute(
                customerId = TestData.CUSTOMER.id
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
        val count = viewEffectObserver.valueCount()
        pushIntent(SubscriptionListContract.Intent.RefreshData)

        verify(listSubscriptions).execute(TestData.CUSTOMER.id)
        viewEffectObserver.awaitCount(count + 1)
        Assert.assertTrue(
            (viewEffectObserver.values().last() is SubscriptionListContract.ViewEvent.NetworkErrorToast)
        )
    }

    @Test
    fun `other error emits generic error`() {
        whenever(
            listSubscriptions.execute(
                customerId = TestData.CUSTOMER.id
            )
        ).thenReturn(
            Observable.just(
                Result.Failure(
                    RuntimeException("")
                )
            )
        )
        val count = viewEffectObserver.valueCount()
        pushIntent(SubscriptionListContract.Intent.RefreshData)

        verify(listSubscriptions).execute(TestData.CUSTOMER.id)
        viewEffectObserver.awaitCount(count + 1)
        Assert.assertTrue(
            (viewEffectObserver.values().last() is SubscriptionListContract.ViewEvent.ServerErrorToast)
        )
    }

    private fun pushIntent(intent: UserIntent) = viewModel.attachIntents(Observable.just(intent))
}
