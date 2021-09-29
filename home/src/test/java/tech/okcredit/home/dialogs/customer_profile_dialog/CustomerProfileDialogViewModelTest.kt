package tech.okcredit.home.dialogs.customer_profile_dialog

import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.collection.contract.GetKycRiskCategory
import `in`.okcredit.collection.contract.GetKycStatus
import `in`.okcredit.collection.contract.KycRisk
import `in`.okcredit.collection.contract.KycRiskCategory
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.contract.GetActiveBusiness
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import tech.okcredit.home.TestData
import tech.okcredit.home.usecase.GetCustomerAndCollectionProfile
import java.util.concurrent.TimeUnit

class CustomerProfileDialogViewModelTest {

    private lateinit var viewModel: CustomerProfileDialogViewModel

    private val initialState = CustomerProfileDialogContract.State()
    private val getActiveBusiness: GetActiveBusiness = mock()
    private val getPaymentReminderIntent: GetPaymentReminderIntent = mock()
    private val navigator: CustomerProfileDialogContract.Navigator = mock()
    private val getCustomerAndCollectionProfile: GetCustomerAndCollectionProfile = mock()
    private val getKycRiskCategory: GetKycRiskCategory = mock()
    private val getKycStatus: GetKycStatus = mock()

    private lateinit var testScheduler: TestScheduler

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        `when`(getActiveBusiness.execute()).thenReturn(Observable.just(TestData.MERCHANT))
        `when`(getCustomerAndCollectionProfile.execute(TestData.CUSTOMER.id))
            .thenReturn(
                Observable.just(
                    GetCustomerAndCollectionProfile.Response(
                        TestData.CUSTOMER,
                        TestData.COLLECTION_CUSTOMER_PROFILE,
                    )
                )
            )

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler
        val customerId = TestData.CUSTOMER.id
        createViewModel(initialState, customerId)
    }

    @Test
    fun `should update merchant value on success of getActiveBusiness during Load`() {

        `when`(getActiveBusiness.execute()).thenReturn(Observable.just(TestData.MERCHANT))

        val testObserver = TestObserver<CustomerProfileDialogContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CustomerProfileDialogContract.Intent.Load))

        // expectations
        assertThat(testObserver.values().last() == initialState.copy(business = TestData.MERCHANT))
        testObserver.dispose()
    }

    @Test
    fun `should not change state on error of getActiveBusiness  during Load`() {
        `when`(getActiveBusiness.execute()).thenReturn(Observable.just(TestData.MERCHANT))

        val testObserver = TestObserver<CustomerProfileDialogContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CustomerProfileDialogContract.Intent.Load))

        // expectations
        assertThat(testObserver.values().last() == initialState)
        testObserver.dispose()
    }

    @Test
    fun `should update customer and collection customer profile during Load`() {
        `when`(getCustomerAndCollectionProfile.execute(TestData.CUSTOMER.id))
            .thenReturn(
                Observable.just(
                    GetCustomerAndCollectionProfile.Response(
                        TestData.CUSTOMER,
                        TestData.COLLECTION_CUSTOMER_PROFILE,
                    )
                )
            )

        val testObserver = TestObserver<CustomerProfileDialogContract.State>()
        viewModel.state().subscribe(testObserver)

        // provide intent
        viewModel.attachIntents(Observable.just(CustomerProfileDialogContract.Intent.Load))

        // expectations
        assertThat(
            testObserver.values().last() == initialState.copy(
                customer = TestData.CUSTOMER,
                collectionCustomerProfile = TestData.COLLECTION_CUSTOMER_PROFILE
            )
        )
        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return no risk `() {
        val status = KycStatus.NOT_SET
        val response = KycRisk(KycRiskCategory.NO_RISK, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        viewModel.attachIntents(Observable.just(CustomerProfileDialogContract.Intent.Load))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)
        val testObserver = viewModel.state().test()

        assertThat(testObserver.values().last().kycStatus).isEqualTo(status)
        assertThat(testObserver.values().last().kycRiskCategory).isEqualTo(response.kycRiskCategory)
        assertThat(testObserver.values().last().isKycLimitReached).isEqualTo(response.isLimitReached)
        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return low risk`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.LOW, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        viewModel.attachIntents(Observable.just(CustomerProfileDialogContract.Intent.Load))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        val testObserver = viewModel.state().test()

        assertThat(testObserver.values().last().kycStatus).isEqualTo(status)
        assertThat(testObserver.values().last().kycRiskCategory).isEqualTo(response.kycRiskCategory)
        assertThat(testObserver.values().last().isKycLimitReached).isEqualTo(response.isLimitReached)
        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return high risk`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.HIGH, false, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        viewModel.attachIntents(Observable.just(CustomerProfileDialogContract.Intent.Load))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        val testObserver = viewModel.state().test()

        assertThat(testObserver.values().last().kycStatus).isEqualTo(status)
        assertThat(testObserver.values().last().kycRiskCategory).isEqualTo(response.kycRiskCategory)
        assertThat(testObserver.values().last().isKycLimitReached).isEqualTo(response.isLimitReached)
        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return low risk with limit reached`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.LOW, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        viewModel.attachIntents(Observable.just(CustomerProfileDialogContract.Intent.Load))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        val testObserver = viewModel.state().test()

        assertThat(testObserver.values().last().kycStatus).isEqualTo(status)
        assertThat(testObserver.values().last().kycRiskCategory).isEqualTo(response.kycRiskCategory)
        assertThat(testObserver.values().last().isKycLimitReached).isEqualTo(response.isLimitReached)

        testObserver.dispose()
    }

    @Test
    fun `getKycRiskDetails() should return high risk with limit reached`() {
        val status = KycStatus.COMPLETE
        val response = KycRisk(KycRiskCategory.HIGH, true, CollectionMerchantProfile.DAILY)
        whenever(getKycStatus.execute()).thenReturn(Observable.just(status))
        whenever(getKycRiskCategory.execute()).thenReturn(Observable.just(response))
        viewModel.attachIntents(Observable.just(CustomerProfileDialogContract.Intent.Load))

        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        val testObserver = viewModel.state().test()

        assertThat(testObserver.values().last().kycStatus).isEqualTo(status)
        assertThat(testObserver.values().last().kycRiskCategory).isEqualTo(response.kycRiskCategory)
        assertThat(testObserver.values().last().isKycLimitReached).isEqualTo(response.isLimitReached)

        testObserver.dispose()
    }

    private fun createViewModel(initialState: CustomerProfileDialogContract.State, customerId: String) {
        viewModel = CustomerProfileDialogViewModel(
            initialState = initialState,
            customerId = customerId,
            getActiveBusiness = { getActiveBusiness },
            getPaymentReminderIntent = { getPaymentReminderIntent },
            navigator = { navigator },
            getCollAndCollectionProfile = { getCustomerAndCollectionProfile },
            getKycRiskCategory = { getKycRiskCategory },
            getKycStatus = { getKycStatus },
            tracker = mock()
        )
    }
}
