package `in`.okcredit.payment.ui.payment_result

import `in`.okcredit.cashback.contract.usecase.CashbackLocalDataOperations
import `in`.okcredit.cashback.contract.usecase.GetCashbackRewardForPayment
import `in`.okcredit.cashback.contract.usecase.IsCustomerCashbackFeatureEnabled
import `in`.okcredit.cashback.contract.usecase.IsSupplierCashbackFeatureEnabled
import `in`.okcredit.collection.contract.GetBlindPayShareLink
import `in`.okcredit.collection.contract.GetCollectionActivationStatus
import `in`.okcredit.payment.analytics.PaymentAnalyticsEvents
import `in`.okcredit.payment.contract.model.JuspayPollingStatus
import `in`.okcredit.payment.contract.model.PaymentModel
import `in`.okcredit.payment.contract.usecase.GetPaymentResult
import `in`.okcredit.payment.usecases.SyncCollectionAndCustomerTransactions
import `in`.okcredit.rewards.contract.GetRewardById
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardsSyncer
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import merchant.okcredit.accounting.contract.model.LedgerType
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.communication.CommunicationRepository
import java.util.concurrent.TimeUnit

class PaymentResultViewModelTest {
    lateinit var testObserver: TestObserver<PaymentResultContract.State>
    private val testObserverViewEvent = TestObserver<PaymentResultContract.ViewEvents>()

    private val initialState = PaymentResultContract.State()
    private val accountId: String = "accountId"
    private val paymentId: String = "paymentId"
    private val paymentType: String = "paymentType"
    private val showTxnCancelled: Boolean = false
    private val mobile: String = "mobile"

    private val communicationApi: CommunicationRepository = mockk()
    private val getPaymentResult: GetPaymentResult = mockk()
    private val getCashbackRewardForPayment: GetCashbackRewardForPayment = mockk()
    private val rewardsSyncer: RewardsSyncer = mockk()
    private val isCustomerCashbackFeatureEnabled: IsCustomerCashbackFeatureEnabled = mockk()
    private val isSupplierCashbackFeatureEnabled: IsSupplierCashbackFeatureEnabled = mockk()
    private val getRewardById: GetRewardById = mockk()
    private val cashbackLocalDataOperations: CashbackLocalDataOperations = mockk()
    private val context: Context = mockk()
    private val getCollectionActivationStatus: GetCollectionActivationStatus = mockk()
    private var paymentAnalyticsEvents: PaymentAnalyticsEvents = mockk()
    private var getBlindPayShareLink: GetBlindPayShareLink = mockk()
    private val syncCollectionAndCustomerTransactions: SyncCollectionAndCustomerTransactions = mockk()
    private lateinit var testScheduler: TestScheduler
    private lateinit var viewModel: PaymentResultViewModel

    private val currentTime = System.currentTimeMillis()
    private val rewardModel = RewardModel(
        id = "reward-WYOWMUZK",
        create_time = DateTime(currentTime),
        update_time = DateTime(currentTime),
        status = "unclaimed/fake",
        amount = 0,
        reward_type = "better_luck_next_time",
        featureName = "",
        featureTitle = "",
        description = "",
        deepLink = "",
        icon = "",
        labels = HashMap(),
        createdBy = "",
    )

    private fun createViewModel(ledgerType: LedgerType = LedgerType.CUSTOMER) = PaymentResultViewModel(
        initialState,
        accountId,
        paymentId,
        paymentType,
        ledgerType.value,
        showTxnCancelled,
        mobile,
        { communicationApi },
        { getPaymentResult },
        { getCashbackRewardForPayment },
        { rewardsSyncer },
        { isCustomerCashbackFeatureEnabled },
        { isSupplierCashbackFeatureEnabled },
        { getRewardById },
        { cashbackLocalDataOperations },
        { context },
        { getCollectionActivationStatus },
        { paymentAnalyticsEvents },
        { getBlindPayShareLink },
        collectionSyncer = { mockk() },
        syncTransactionsImpl = { mockk() },
        getCustomerSupportType = mockk(),
        getCustomerSupportData = mockk(),
        syncCollectionAndCustomerTransactions = { syncCollectionAndCustomerTransactions },
    )

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()
        every { Schedulers.single() } returns Schedulers.trampoline()

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler
    }

//  TODO: Fix tests
/*    @Test
    fun `verify intent LoadRewardForPayment when accountType is Customer`() {
        viewModel = createViewModel(AccountType.CUSTOMER)
        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(testObserverViewEvent)

        val dummyPaymentId = "DUMMY_PAYMENT_ID"
        val updatedRewardModel = rewardModel.copy(status = "claimed")
        val subject = BehaviorSubject.createDefault(rewardModel)

        every { isCustomerCashbackFeatureEnabled.execute() } returns (Observable.just(true))
        every { getCashbackRewardForPayment.execute(dummyPaymentId) } returns (Observable.just(rewardModel))
        every { runBlocking { rewardsSyncer.syncRewards() } } returns (listOf(rewardModel))
        every { getRewardById.execute(rewardModel.id) } returns (subject)

        viewModel.attachIntents(Observable.just(PaymentResultContract.Intent.LoadRewardForPayment(dummyPaymentId)))
//        TODO: Fix this
//        runBlocking {
//            delay(30)
//            testObserverViewEvent.assertValue(PaymentResultContract.ViewEvents.GoToClaimRewardScreen(rewardModel))
//        }

        assertThat(testObserver.values()[0] == initialState).isTrue()

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)
        assertThat(testObserver.values()[1] == initialState.copy(reward = rewardModel)).isTrue()

        subject.onNext(updatedRewardModel)
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertThat(testObserver.values()[2] == initialState.copy(reward = updatedRewardModel)).isTrue()
    }

    @Test
    fun `verify intent LoadRewardForPayment when accountType is Supplier`() {
        viewModel = createViewModel(AccountType.SUPPLIER)
        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(testObserverViewEvent)

        val dummyPaymentId = "DUMMY_PAYMENT_ID"
        val updatedRewardModel = rewardModel.copy(status = "claimed")
        val subject = BehaviorSubject.createDefault(rewardModel)

        every { isSupplierCashbackFeatureEnabled.execute() } returns (Observable.just(true))
        every { getCashbackRewardForPayment.execute(dummyPaymentId) } returns (Observable.just(rewardModel))
        every { runBlocking { rewardsSyncer.syncRewards() } } returns (listOf(rewardModel))
        every { getRewardById.execute(rewardModel.id) } returns (subject)

        viewModel.attachIntents(Observable.just(PaymentResultContract.Intent.LoadRewardForPayment(dummyPaymentId)))
//        TODO: Fix this
//        runBlocking {
//            delay(30)
//            testObserverViewEvent.assertValue(PaymentResultContract.ViewEvents.GoToClaimRewardScreen(rewardModel))
//        }

        assertThat(testObserver.values()[0] == initialState).isTrue()

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)
        assertThat(testObserver.values()[1] == initialState.copy(reward = rewardModel)).isTrue()

        subject.onNext(updatedRewardModel)
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertThat(testObserver.values()[2] == initialState.copy(reward = updatedRewardModel)).isTrue()
    }

    @Test
    fun `verify intent LoadRewardForPayment when accountType is Customer and cashback not enabled`() {
        viewModel = createViewModel(AccountType.CUSTOMER)
        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(testObserverViewEvent)

        val dummyPaymentId = "DUMMY_PAYMENT_ID"

        every { isCustomerCashbackFeatureEnabled.execute() } returns (Observable.just(false))

        viewModel.attachIntents(Observable.just(PaymentResultContract.Intent.LoadRewardForPayment(dummyPaymentId)))
//        TODO: Fix this
//        runBlocking {
//            delay(30)
//            assertThat(testObserverViewEvent.valueCount() == 0).isTrue()
//        }

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)
        assertTrue(testObserver.values().last() == initialState)
    }*/

    @Test
    fun `verify intent LoadData clears cashback cache when response is success`() {
        viewModel = createViewModel()
        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(testObserverViewEvent)

        val juspayPaymentPollingModel = PaymentModel.JuspayPaymentPollingModel(
            status = JuspayPollingStatus.SUCCESS.value,
            paymentId = "",
            paymentInfo = mockk(),
        )

        val observable = Observable.just(juspayPaymentPollingModel)

        mockkObject(UseCase.Companion)
        every { UseCase.Companion.wrapObservable(observable) } answers {
            Observable.just(Result.Success(juspayPaymentPollingModel))
        }
        every { getPaymentResult.execute(paymentId, true, paymentType) } returns (observable)
        every { cashbackLocalDataOperations.executeInvalidateLocalData() } returns (Completable.complete())

        viewModel.attachIntents(Observable.just(PaymentResultContract.Intent.LoadData))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)
        verify { cashbackLocalDataOperations.executeInvalidateLocalData() }
    }

    @Test
    fun `verify intent LoadData clears cashback cache when response is other than success`() {
        viewModel = createViewModel()
        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(testObserverViewEvent)

        JuspayPollingStatus.values().forEach label@{
            if (it.value == JuspayPollingStatus.SUCCESS.value) return@label
            val juspayPaymentPollingModel = PaymentModel.JuspayPaymentPollingModel(
                status = it.value,
                paymentId = "",
                paymentInfo = mockk(),
            )

            val observable = Observable.just(juspayPaymentPollingModel)

            mockkObject(UseCase.Companion)
            every { UseCase.Companion.wrapObservable(observable) } answers {
                Observable.just(Result.Success(juspayPaymentPollingModel))
            }
            every { getPaymentResult.execute(paymentId, true, paymentType) } returns (observable)
            every { cashbackLocalDataOperations.executeInvalidateLocalData() } returns (Completable.complete())

            viewModel.attachIntents(Observable.just(PaymentResultContract.Intent.LoadData))

            testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)
            verify { cashbackLocalDataOperations.executeInvalidateLocalData() wasNot called }
        }
    }

    @After
    fun cleanUp() {
        testObserver.dispose()
        testObserverViewEvent.dispose()
    }
}
