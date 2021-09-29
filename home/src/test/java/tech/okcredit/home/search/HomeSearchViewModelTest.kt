package tech.okcredit.home.search

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.usecase.AddCustomer
import `in`.okcredit.backend._offline.usecase.AddSupplier
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.collection.contract.GetKycRiskCategory
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import tech.okcredit.base.network.NetworkError
import tech.okcredit.home.TestData
import tech.okcredit.home.ui.homesearch.HomeSearchContract
import tech.okcredit.home.ui.homesearch.HomeSearchViewModel
import java.io.IOException

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class HomeSearchViewModelTest {

    lateinit var viewModel: HomeSearchViewModel

    private val addCustomer: AddCustomer = mock()

    private val addSupplier: AddSupplier = mock()

    private val getKycRiskCategory: GetKycRiskCategory = mock()

    private val getPaymentReminderIntent: GetPaymentReminderIntent = mock()

    private val tracker: Tracker = mock()

    private val initialState = HomeSearchContract.State()

    lateinit var testObserver: TestObserver<HomeSearchContract.State>

    private val viewEffectObserver = TestObserver<HomeSearchContract.ViewEvent>()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        // create Presenter
        createViewModel(initialState, TestData.source_supplier)

        // observe state
        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(viewEffectObserver)

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @After
    fun close() {
        testObserver.dispose()
        viewEffectObserver.dispose()
    }

    @Test
    fun `default source is customer tab`() {
        // setup
        viewModel.attachIntents(Observable.just(HomeSearchContract.Intent.Load))

        // expectations
        assertThat(testObserver.values().first().source == TestData.source_customer)
    }

    @Test
    fun `source is set if provided as param`() {
        // setup
        viewModel.attachIntents(Observable.just(HomeSearchContract.Intent.Load))

        // expectations
        assertThat(testObserver.values().last().source == TestData.source_supplier)
    }

    @Test
    fun `search query is updated`() {
        // setup
        viewModel.attachIntents(Observable.just(HomeSearchContract.Intent.SearchQuery(TestData.SEARCH_QUERY)))

        // expectations
        assertThat(testObserver.values().first().searchQuery != TestData.SEARCH_QUERY)
        assertThat(testObserver.values().last().searchQuery == TestData.SEARCH_QUERY)
    }

    /****************************************************************
     * Add Relation Success Cases
     ****************************************************************/

    @Test
    fun `Add Relation From Contact on customer tab should navigate to customer screen after success`() {

        // setup
        `when`(
            addCustomer.execute(
                TestData.CONTACT.name,
                TestData.CONTACT.mobile,
                TestData.CONTACT.picUri
            )
        )
            .thenReturn(Single.just(TestData.CUSTOMER))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromContact(
                    TestData.CONTACT,
                    TestData.source_customer
                )
            )
        )

        verify(tracker).trackAddRelationshipSuccessFlows(
            relation = PropertyValue.CUSTOMER,
            accountId = TestData.CUSTOMER.id,
            search = PropertyValue.TRUE,
            contact = PropertyValue.TRUE
        )

        // expectations
        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.GoToCustomerScreenAndCloseSearch(
                    TestData.CUSTOMER.id, TestData.CUSTOMER.mobile
                )
        ).isTrue()

        testObserver.dispose()
    }

    @Test
    fun `Add Relation From Contact on supplier tab should navigate to supplier screen after success`() {
        // setup
        `when`(
            addSupplier.execute(
                AddSupplier.Request(
                    TestData.CONTACT.name,
                    TestData.CONTACT.mobile,
                    TestData.CONTACT.picUri
                )
            )
        )
            .thenReturn(Single.just(TestData.SUPPLIER))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromContact(
                    TestData.CONTACT,
                    TestData.source_supplier
                )
            )
        )

        // expectations
        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.GoToSupplierScreenAndCloseSearch(
                    TestData.SUPPLIER
                )
        )
    }

    @Test
    fun `Add Relation From Search on supplier tab should navigate to supplier screen after success`() {
        // setup
        `when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.just(TestData.CUSTOMER))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY,
                    TestData.source_customer
                )
            )
        )

        // expectations
        verify(tracker).trackAddRelationshipSuccessFlows(
            relation = PropertyValue.CUSTOMER,
            accountId = TestData.CUSTOMER.id,
            search = PropertyValue.TRUE,
            contact = PropertyValue.FALSE
        )
        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.GoToCustomerScreenAndCloseSearch(
                    TestData.CUSTOMER.id, TestData.CUSTOMER.mobile
                )
        ).isTrue()
    }

    @Test
    fun `Add Relation From Search on customer tab should navigate to customer screen after success`() {

        `when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.just(TestData.CUSTOMER))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY,
                    TestData.source_customer
                )
            )
        )

        // expectations
        verify(tracker).trackAddRelationshipSuccessFlows(
            relation = PropertyValue.CUSTOMER,
            accountId = TestData.CUSTOMER.id,
            search = PropertyValue.TRUE,
            contact = PropertyValue.FALSE
        )

        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.GoToCustomerScreenAndCloseSearch(
                    TestData.CUSTOMER.id, TestData.CUSTOMER.mobile
                )
        )
    }

    /****************************************************************
     * Add Customer Error Test Cases
     ****************************************************************/

    @Test
    fun `Add Relation on customer should show invalid mobile number error on invalid mobile`() {

        `when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(CustomerErrors.InvalidMobile()))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY,
                    TestData.source_customer
                )
            )
        )

        // expectations
        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.ShowInvalidMobileNumber
        )
    }

    @Test
    fun `Add Relation on customer should show mobile conflict error on mobile conflict`() {

        `when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(CustomerErrors.MobileConflict(TestData.CUSTOMER)))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY,
                    TestData.source_customer
                )
            )
        )

        // expectations
        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.ShowMobileConflictForCustomer(TestData.CUSTOMER)
        )
    }

    @Test
    fun `Add Relation on customer should show invalid name if invalid name exception`() {

        `when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(CustomerErrors.InvalidName()))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY,
                    TestData.source_customer
                )
            )
        )

        // expectations
        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.ShowInvalidName
        )
    }

    @Test
    fun `Add Relation on customer should internet error on Network Error`() {
        `when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(NetworkError(cause = IOException())))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY,
                    TestData.source_customer
                )
            )
        )

        // expectations
        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.ShowInternetError
        )
    }

    @Test
    fun `Add Relation on customer should show error on Common errors`() {

        `when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        ).thenReturn(Single.error(Exception()))

        viewModel.attachIntents(
            Observable.just(
                HomeSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY,
                    TestData.source_customer
                )
            )
        )

        // expectations
        assertThat(
            viewEffectObserver.values().last() ==
                HomeSearchContract.ViewEvent.ShowError
        )
    }

    private fun createViewModel(initialState: HomeSearchContract.State, source: HomeSearchContract.SOURCE) {
        viewModel = HomeSearchViewModel(
            initialState = { initialState },
            isAccountSelection = { false },
            source = { source.value },
            getHomeSearchData = mock(),
            getSupplierCreditEnabledCustomerIds = mock(),
            getUnSyncedCustomers = mock(),
            isPermissionGranted = mock(),
            addCustomer = { addCustomer },
            addSupplier = { addSupplier },
            tracker = { tracker },
            getUnSyncedSuppliers = mock(),
            contactsRepository = { mock() },
            getKycRiskCategory = { getKycRiskCategory },
            getPaymentReminderIntent = { getPaymentReminderIntent },
            abRepository = mock()
        )
    }
}
