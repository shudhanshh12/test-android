package `in`.okcredit.collection_ui.ui.merchant_destination

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection_ui.ui.insights.CollectionInsightsContract
import `in`.okcredit.collection_ui.ui.insights.MerchantDestinationViewModel
import `in`.okcredit.collection_ui.usecase.*
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.AuthService
import java.util.concurrent.TimeUnit

class MerchantDestinationViewModelTest {
    private lateinit var testObserver: TestObserver<CollectionInsightsContract.State>
    private lateinit var merchantDestinationViewModel: MerchantDestinationViewModel
    private lateinit var eventObserver: TestObserver<CollectionInsightsContract.ViewEvent>

    private val initialState: CollectionInsightsContract.State = CollectionInsightsContract.State()
    private val getCollectionMerchantProfileImpl: GetCollectionMerchantProfileImpl = mock()
    private val getActiveBusiness: GetActiveBusiness = mock()
    private val navigator: CollectionInsightsContract.Navigator = mock()
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val tracker: Tracker = mock()
    private val setCollectionDestination: SetCollectionDestinationImpl = mock()
    private val getCreditGraphicalData: GetCreditGraphicalData = mock()
    private val getAllDueCustomersByLastPayment: GetAllDueCustomersByLastPayment = mock()
    private val getPaymentReminderIntent: GetPaymentReminderIntent = mock()
    private val context: Context = mock()
    private val getCustomerCollectionProfileImpl: GetCustomerCollectionProfileImpl = mock()
    private val getReferralLink: GetReferralLink = mock()
    private val authService: AuthService = mock()

    private val getMerchantQRIntent: GetMerchantQRIntent = mock()
    private val getMerchantQRIntentLazy: Lazy<GetMerchantQRIntent> = mock()

    private val saveMerchantQROnDevice: SaveMerchantQROnDevice = mock()
    private val saveMerchantQROnDeviceLazy: Lazy<SaveMerchantQROnDevice> = mock()

    private val getKycStatus: GetKycStatus = mock()
    private val getKycRiskCategory: GetKycRiskCategory = mock()
    private val scheduleSyncCollections: ScheduleSyncCollections = mock()

    private lateinit var testScheduler: TestScheduler

    fun createViewModel(initialState: CollectionInsightsContract.State) {
        merchantDestinationViewModel = MerchantDestinationViewModel(
            initialState = initialState,
            navigator = navigator,
            getCollectionMerchantProfile = getCollectionMerchantProfileImpl,
            getActiveBusiness = getActiveBusiness,
            checkNetworkHealth = checkNetworkHealth,
            tracker = tracker,
            setCollectionDestination = setCollectionDestination,
            getCreditGraphicalData = getCreditGraphicalData,
            getAllDueCustomersByLastPayment = getAllDueCustomersByLastPayment,
            getPaymentReminderIntent = getPaymentReminderIntent,
            context = context,
            getCustomerCollectionProfile = getCustomerCollectionProfileImpl,
            getReferralLink = getReferralLink,
            authService = authService,
            getKycStatus = { getKycStatus },
            getKycRiskCategory = { getKycRiskCategory },
            scheduleSyncCollections = { scheduleSyncCollections }
        )
    }

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        createViewModel(initialState)
        whenever(getMerchantQRIntentLazy.get()).thenReturn(getMerchantQRIntent)
        whenever(saveMerchantQROnDeviceLazy.get()).thenReturn(saveMerchantQROnDevice)

        testObserver = merchantDestinationViewModel.state().test()
        eventObserver = merchantDestinationViewModel.viewEvent().test()
    }

    @Test
    fun `SelectGraphDuration() returns success `() {
        val request = CreditGraphicalDataProvider.GraphDuration.WEEK
        val response: CreditGraphicalDataProvider.GraphResponse = mock()
        whenever(getCreditGraphicalData.execute(request))
            .thenReturn(Observable.just(response))
        // provide intent
        merchantDestinationViewModel.attachIntents(
            Observable.just(
                CollectionInsightsContract.Intent.SelectGraphDuration(request)
            )
        )
        // expectations
        assertThat(testObserver.values().first() == initialState)
        assertThat(
            testObserver.values().last() == initialState.copy(graphResponse = response)
        )
    }

    @Test
    fun `SelectGraphDuration() returns failure`() {
        val request = CreditGraphicalDataProvider.GraphDuration.WEEK
        whenever(getCreditGraphicalData.execute(request)).thenReturn(Observable.error(Throwable("error")))
        // provide intent
        merchantDestinationViewModel.attachIntents(
            Observable.just(
                CollectionInsightsContract.Intent.SelectGraphDuration(request)
            )
        )
        // expectations
        assertThat(testObserver.values().first() == initialState)
        assertThat(testObserver.values().last() == initialState)
    }

    @Test
    fun `HidePaymentReminderDialog()  event `() {
        merchantDestinationViewModel.attachIntents(
            Observable.just(
                CollectionInsightsContract.Intent.HidePaymentReminderDialog
            )
        )
        // expectations
        assertThat(testObserver.values().first() == initialState)
        assertThat(
            testObserver.values().last() == initialState
        )
    }

    @Test
    fun `getKycRiskDetails() should return no risk `() {
        val status = KycStatus.NOT_SET
        val response = KycRisk(KycRiskCategory.NO_RISK, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        merchantDestinationViewModel.attachIntents(Observable.just(CollectionInsightsContract.Intent.LoadKycDetails))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertThat(testObserver.values().contains(initialState)).isTrue()
        assertThat(testObserver.values().last()).isEqualTo(
            initialState.copy(
                kycStatus = status,
                kycRiskCategory = response.kycRiskCategory,
                isLimitReached = response.isLimitReached
            )
        )
        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return low risk`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.LOW, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        merchantDestinationViewModel.attachIntents(Observable.just(CollectionInsightsContract.Intent.LoadKycDetails))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertThat(testObserver.values().contains(initialState)).isTrue()
        assertThat(testObserver.values().last()).isEqualTo(
            initialState.copy(
                kycStatus = status,
                kycRiskCategory = response.kycRiskCategory,
                isLimitReached = response.isLimitReached
            )
        )
        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return high risk`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.HIGH, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        merchantDestinationViewModel.attachIntents(Observable.just(CollectionInsightsContract.Intent.LoadKycDetails))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertThat(testObserver.values().contains(initialState)).isTrue()
        assertThat(testObserver.values().last()).isEqualTo(
            initialState.copy(
                kycStatus = status,
                kycRiskCategory = response.kycRiskCategory,
                isLimitReached = response.isLimitReached
            )
        )
        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return low risk with limit reached`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.LOW, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        merchantDestinationViewModel.attachIntents(Observable.just(CollectionInsightsContract.Intent.LoadKycDetails))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertThat(testObserver.values().contains(initialState)).isTrue()
        assertThat(testObserver.values().last()).isEqualTo(
            initialState.copy(
                kycStatus = status,
                kycRiskCategory = response.kycRiskCategory,
                isLimitReached = response.isLimitReached
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return high risk with limit reached`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.HIGH, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        merchantDestinationViewModel.attachIntents(Observable.just(CollectionInsightsContract.Intent.LoadKycDetails))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertThat(testObserver.values().contains(initialState)).isTrue()
        assertThat(testObserver.values().last()).isEqualTo(
            initialState.copy(
                kycStatus = status,
                kycRiskCategory = response.kycRiskCategory,
                isLimitReached = response.isLimitReached
            )
        )

        testObserver.dispose()
    }

    @After
    fun cleanup() {
        testObserver.dispose()
    }
}
