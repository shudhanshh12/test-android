package tech.okcredit.home.ui.add_transaction_home_search

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.error.CustomerErrors
import `in`.okcredit.backend._offline.usecase.AddCustomer
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
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
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.mockito.Mockito
import tech.okcredit.android.auth.Unauthorized
import tech.okcredit.base.network.NetworkError
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.home.TestData
import tech.okcredit.home.ui.homesearch.HomeSearchContract
import java.io.IOException

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AddTransactionShortcutSearchViewModelTest {

    lateinit var viewModel: AddTransactionShortcutSearchViewModel

    private val addCustomer: AddCustomer = mock()

    private val tracker: Tracker = mock()

    private val initialState = AddTransactionShortcutSearchContract.State()

    lateinit var testObserver: TestObserver<AddTransactionShortcutSearchContract.State>

    private val viewEffectObserver = TestObserver<AddTransactionShortcutSearchContract.ViewEvent>()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        // create Presenter
        createViewModel(initialState)

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
    fun `search query is updated`() {
        // setup
        viewModel.attachIntents(Observable.just(HomeSearchContract.Intent.SearchQuery(TestData.SEARCH_QUERY)))

        // expectations
        Truth.assertThat(testObserver.values().first().searchQuery != TestData.SEARCH_QUERY)
        Truth.assertThat(testObserver.values().last().searchQuery == TestData.SEARCH_QUERY)
    }

    /****************************************************************
     * Add Relation Success Cases
     ****************************************************************/

    @Test
    fun `Add Relation From Contact on customer tab should navigate to customer screen after success`() {

        // setup
        Mockito.`when`(
            addCustomer.execute(
                TestData.CONTACT.name,
                TestData.CONTACT.mobile,
                TestData.CONTACT.picUri
            )
        )
            .thenReturn(Single.just(TestData.CUSTOMER))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromContact(
                    TestData.CONTACT
                )
            )
        )

        Mockito.verify(tracker).trackAddRelationshipSuccessFlows(
            relation = PropertyValue.CUSTOMER,
            accountId = TestData.CUSTOMER.id,
            search = PropertyValue.TRUE,
            contact = PropertyValue.TRUE
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.GotoCustomerScreenAndCloseSearch(
                    TestData.CUSTOMER.id, TestData.CUSTOMER.mobile
                )
        ).isTrue()

        testObserver.dispose()
    }

    @Test
    fun `Add Relation From Search on supplier tab should navigate to supplier screen after success`() {
        // setup
        Mockito.`when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.just(TestData.CUSTOMER))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY
                )
            )
        )

        // expectations
        Mockito.verify(tracker).trackAddRelationshipSuccessFlows(
            relation = PropertyValue.CUSTOMER,
            accountId = TestData.CUSTOMER.id,
            search = PropertyValue.TRUE,
            contact = PropertyValue.FALSE
        )
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.GotoCustomerScreenAndCloseSearch(
                    TestData.CUSTOMER.id, TestData.CUSTOMER.mobile
                )
        ).isTrue()
    }

    @Test
    fun `Add Relation From Search on customer tab should navigate to customer screen after success`() {

        Mockito.`when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.just(TestData.CUSTOMER))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY
                )
            )
        )

        // expectations
        Mockito.verify(tracker).trackAddRelationshipSuccessFlows(
            relation = PropertyValue.CUSTOMER,
            accountId = TestData.CUSTOMER.id,
            search = PropertyValue.TRUE,
            contact = PropertyValue.FALSE
        )

        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.GotoCustomerScreenAndCloseSearch(
                    TestData.CUSTOMER.id, TestData.CUSTOMER.mobile
                )
        )
    }

    /****************************************************************
     * Add Customer Error Test Cases
     ****************************************************************/

    @Test
    fun `Add Relation on customer should show invalid mobile number error on invalid mobile`() {

        Mockito.`when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(CustomerErrors.InvalidMobile()))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY
                )
            )
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.ShowInvalidMobileNumber
        )
    }

    @Test
    fun `Add Relation on customer should show mobile conflict error on mobile conflict`() {

        Mockito.`when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(CustomerErrors.MobileConflict(TestData.CUSTOMER)))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY
                )
            )
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.ShowMobileConflictForCustomer(TestData.CUSTOMER)
        )
    }

    @Test
    fun `Add Relation on customer should show invalid name if invalid name exception`() {

        Mockito.`when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(CustomerErrors.InvalidName()))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY
                )
            )
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.ShowInvalidName
        )
    }

    @Test
    fun `Add Relation on customer should navigate to login `() {

        Mockito.`when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(Unauthorized()))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY
                )
            )
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.GotoLogin
        )
    }

    @Test
    fun `Add Relation on customer should internet error on Network Error`() {
        Mockito.`when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        )
            .thenReturn(Single.error(NetworkError(cause = IOException())))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY
                )
            )
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.ShowInternetError
        )
    }

    @Test
    fun `Add Relation on customer should show error on Common errors`() {

        Mockito.`when`(
            addCustomer.execute(
                TestData.SEARCH_QUERY,
                null,
                null
            )
        ).thenReturn(Single.error(Exception()))

        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.AddRelationFromSearch(
                    TestData.SEARCH_QUERY
                )
            )
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.ShowError
        )
    }

    @Test
    fun `Click on close icon should close app`() {
        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.OnBackPressed
            )
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.OnBackPressed
        )
    }

    @Test
    fun `Click on home icon should open home screen`() {
        viewModel.attachIntents(
            Observable.just(
                AddTransactionShortcutSearchContract.Intent.GoToHomeScreen
            )
        )

        // expectations
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                AddTransactionShortcutSearchContract.ViewEvent.GotoHomeScreen
        )
    }

    private fun createViewModel(initialState: AddTransactionShortcutSearchContract.State) {
        viewModel = AddTransactionShortcutSearchViewModel(
            initialState = { initialState },
            getHomeSearchData = mock(),
            getSupplierCreditEnabledCustomerIds = mock(),
            getUnSyncedCustomers = mock(),
            isPermissionGranted = mock(),
            addCustomer = { addCustomer },
            tracker = { tracker },
            getUnSyncedSuppliers = mock(),
            contactsRepository = { mock<ContactsRepository>() },
            collectionRepository = mock(),
            getActiveBusiness = mock(),
            getPaymentReminderIntent = mock(),
            authService = mock(),
            getSuggestedCustomersForAddTransactionShortcut = mock(),
            ab = mock(),
        )
    }
}
