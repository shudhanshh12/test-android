package `in`.okcredit.merchant.customer_ui.ui.subscription.add

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.ui.subscription.usecase.AddSubscription
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
import java.util.concurrent.TimeUnit

class AddSubscriptionViewModelTest {

    private lateinit var viewModel: AddSubscriptionViewModel

    private val getCustomer: GetCustomer = mock()
    private val addSubscription: AddSubscription = mock()

    private lateinit var testScheduler: TestScheduler
    private lateinit var testObserver: TestObserver<AddSubscriptionContract.State>
    private lateinit var viewEffectObserver: TestObserver<AddSubscriptionContract.ViewEvent>

    private val name = TestData.DAILY_SUBSCRIPTION.name

    private val amount = "1,000"
    private val amountCalculation = TestData.DAILY_SUBSCRIPTION.amount

    private fun createPresenter(initialState: AddSubscriptionContract.State) {
        viewModel = AddSubscriptionViewModel(
            initialState = { initialState },
            customerId = TestData.CUSTOMER.id,
            getCustomer = { getCustomer },
            addSubscription = { addSubscription }
        )
    }

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()
        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        val initialState = AddSubscriptionContract.State()
        createPresenter(initialState)
        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler
        testObserver = viewModel.state().test()
        viewEffectObserver = viewModel.viewEvent().test()
    }

    @Test
    fun `load intent checks initial frequency and gets customer`() {

        whenever(getCustomer.execute(TestData.CUSTOMER.id)).thenReturn(Observable.just(TestData.CUSTOMER))
        val initialState = AddSubscriptionContract.State()
        val presenter = AddSubscriptionViewModel(
            initialState = { initialState },
            customerId = TestData.CUSTOMER.id,
            getCustomer = { getCustomer },
            addSubscription = { addSubscription }
        )

        val testObserver = presenter.state().test()
        presenter.attachIntents(Observable.just(AddSubscriptionContract.Intent.Load))

        verify(getCustomer).execute(TestData.CUSTOMER.id)
        Assert.assertTrue(presenter.selectedFrequency == SubscriptionFrequency.DAILY)

        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        Assert.assertTrue(
            testObserver.values().last() == initialState.copy(
                customer = TestData.CUSTOMER,
                selectedFrequency = SubscriptionFrequency.DAILY
            )
        )
    }

    @Test
    fun `add name clicked fires go to add name`() {
        val currentState = testObserver.values().last()
        // push add name clicked intent
        pushIntent(AddSubscriptionContract.Intent.AddNameClicked)
        // check for correct view event to be fired
        Assert.assertTrue(
            (viewEffectObserver.values().last() is AddSubscriptionContract.ViewEvent.GoToAddName)
        )
        // check that no change in current state
        Assert.assertTrue(testObserver.values().last() == currentState)
    }

    @Test
    fun `add frequency clicked fires go to add frequency`() {
        val currentState = testObserver.values().last()
        // push add name clicked intent
        pushIntent(AddSubscriptionContract.Intent.AddFrequencyClicked)
        // check for correct view event to be fired
        Assert.assertTrue(
            (viewEffectObserver.values().last() is AddSubscriptionContract.ViewEvent.GoToAddFrequency)
        )
        // check that no change in current state
        Assert.assertTrue(testObserver.values().last() == currentState)
    }

    @Test
    fun `name added`() {
        val currentState = testObserver.values().last()
        // push name added intent
        pushIntent(AddSubscriptionContract.Intent.NameAdded(name))
        // check name added in presenter
        Assert.assertTrue(viewModel.name == name)
        // check that change in current state
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        println("last value = ${testObserver.values().last().name}")
        Assert.assertTrue(testObserver.values().contains(currentState.copy(name = name)))
    }

    @Test
    fun `frequency added`() {
        val currentState = testObserver.values().last()
        // push name added intent
        pushIntent(
            AddSubscriptionContract.Intent.FrequencyAdded(
                SubscriptionFrequency.DAILY,
                daysInWeek = null,
                startDate = null
            )
        )
        // check name added in presenter
        Assert.assertTrue(viewModel.selectedFrequency == SubscriptionFrequency.DAILY)
        // check that change in current state
        println("last value = ${testObserver.values().last().name}")
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        Assert.assertTrue(
            testObserver.values().contains(currentState.copy(selectedFrequency = SubscriptionFrequency.DAILY))
        )
    }

    @Test
    fun `calculator data added`() {
        val currentState = testObserver.values().last()
        // push name added intent
        pushIntent(AddSubscriptionContract.Intent.CalculatorData(amount, amountCalculation))

        // check amount set in presenter param
        assert(viewModel.amount == amountCalculation)

        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)
        // check that change in current state
        Assert.assertTrue(
            testObserver.values().contains(currentState.copy(amount = amountCalculation, amountCalculation = amount))
        )
    }

    @Test
    fun `submit clicked adds subscription`() {
        whenever(
            addSubscription.execute(
                customerId = viewModel.customerId!!,
                amount = amountCalculation,
                name = name,
                frequency = SubscriptionFrequency.DAILY,
                startDate = TestData.DAILY_SUBSCRIPTION.startDate / 1000,
                days = null
            )
        ).thenReturn(
            Observable.just(
                Result.Success(TestData.DAILY_SUBSCRIPTION)
            )
        )
        viewModel.name = name
        viewModel.amount = amountCalculation
        viewModel.startDate = TestData.DAILY_SUBSCRIPTION.startDate
        viewModel.selectedFrequency = SubscriptionFrequency.DAILY
        val count = viewEffectObserver.valueCount()

        pushIntent(AddSubscriptionContract.Intent.SubmitClicked)

        verify(addSubscription).execute(
            customerId = viewModel.customerId!!,
            amount = amountCalculation,
            name = name,
            frequency = SubscriptionFrequency.DAILY,
            startDate = TestData.DAILY_SUBSCRIPTION.startDate / 1000,
            days = null
        )

        viewEffectObserver.awaitCount(count + 1)
        Assert.assertTrue(
            (viewEffectObserver.values().last() is AddSubscriptionContract.ViewEvent.Success)
        )
    }

    @Test
    fun `network error emits internet issue view event`() {
        whenever(
            addSubscription.execute(
                customerId = viewModel.customerId!!,
                amount = amountCalculation,
                name = name,
                frequency = SubscriptionFrequency.DAILY,
                startDate = TestData.DAILY_SUBSCRIPTION.startDate / 1000,
                days = null
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
        viewModel.name = name
        viewModel.amount = amountCalculation
        viewModel.startDate = TestData.DAILY_SUBSCRIPTION.startDate
        viewModel.selectedFrequency = SubscriptionFrequency.DAILY
        val count = viewEffectObserver.valueCount()

        pushIntent(AddSubscriptionContract.Intent.SubmitClicked)

        verify(addSubscription).execute(
            customerId = viewModel.customerId!!,
            amount = amountCalculation,
            name = name,
            frequency = SubscriptionFrequency.DAILY,
            startDate = TestData.DAILY_SUBSCRIPTION.startDate / 1000,
            days = null
        )

        viewEffectObserver.awaitCount(count + 1)
        Assert.assertTrue(
            (viewEffectObserver.values().last() is AddSubscriptionContract.ViewEvent.ShowError)
        )
    }

    @Test
    fun `other error emits generic error`() {
        whenever(
            addSubscription.execute(
                customerId = viewModel.customerId!!,
                amount = amountCalculation,
                name = name,
                frequency = SubscriptionFrequency.DAILY,
                startDate = TestData.DAILY_SUBSCRIPTION.startDate / 1000,
                days = null
            )
        ).thenReturn(
            Observable.just(
                Result.Failure(
                    RuntimeException("")
                )
            )
        )
        viewModel.name = name
        viewModel.amount = amountCalculation
        viewModel.startDate = TestData.DAILY_SUBSCRIPTION.startDate
        viewModel.selectedFrequency = SubscriptionFrequency.DAILY
        val count = viewEffectObserver.valueCount()

        pushIntent(AddSubscriptionContract.Intent.SubmitClicked)

        verify(addSubscription).execute(
            customerId = viewModel.customerId!!,
            amount = amountCalculation,
            name = name,
            frequency = SubscriptionFrequency.DAILY,
            startDate = TestData.DAILY_SUBSCRIPTION.startDate / 1000,
            days = null
        )

        viewEffectObserver.awaitCount(count + 1)
        Assert.assertTrue(
            (viewEffectObserver.values().last() is AddSubscriptionContract.ViewEvent.ShowError)
        )
    }

    @After
    fun cleanup() {
        testObserver.dispose()
        viewEffectObserver.dispose()
    }

    private fun pushIntent(intent: UserIntent) = viewModel.attachIntents(Observable.just(intent))
}
