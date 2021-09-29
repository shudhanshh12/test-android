package tech.okcredit.home.ui.activity

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Signout
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.home.ui.activity.HomeActivityContract.Intent.Load
import tech.okcredit.home.ui.activity.HomeActivityContract.State
import tech.okcredit.home.ui.activity.HomeActivityContract.ViewEvent
import tech.okcredit.home.usecase.IsMenuOnBottomNavigationEnabled

class HomeActivityViewModelTest {

    private val ab: AbRepository = mock()
    private val signOut: Signout = mock()
    private val tracker: Tracker = mock()
    private val authService: AuthService = mock()
    private val rxSharedPreference: DefaultPreferences = mock()
    private lateinit var viewModel: HomeActivityViewModel
    private val isMenuOnBottomNavigationEnabled: IsMenuOnBottomNavigationEnabled = mock()

    private val initialState = State()
    lateinit var testObserver: TestObserver<State>
    private val viewEffectObserver = TestObserver<ViewEvent>()

    private fun createViewModel(initialState: State) {
        viewModel = HomeActivityViewModel(
            initialState = { initialState },
            signOut = { signOut },
            tracker = { tracker },
            authService = { authService },
            rxSharedPreference = { rxSharedPreference },
            webUrl = "webUrl",
            getMenuExperimentAndDashboardFeature = mock()
        )
    }

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined

        createViewModel(initialState)

        testObserver = viewModel.state().test()
        viewModel.viewEvent().subscribe(viewEffectObserver)

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `auth service returning false should emit go to login view event`() {
        whenever(authService.authState()).thenReturn(Observable.just(false))
        whenever(signOut.isInProgress()).thenReturn(false)

        viewModel.attachIntents(Observable.just(HomeActivityContract.Intent.OnResume))

        assertThat(viewEffectObserver.values().last() == ViewEvent.GoToLogin).isTrue()
    }

    @Test
    fun `auth service returning true should not emit go to login view event`() {
        whenever(authService.authState())
            .thenReturn(Observable.just(true))

        viewModel.attachIntents(Observable.just(Load))

        assertThat(viewEffectObserver.values().contains(ViewEvent.GoToLogin)).isFalse()
    }

    @Test
    fun `rx preference boolean intent should call put boolean`() {
        runBlocking {
            viewModel.attachIntents(Observable.just(HomeActivityContract.Intent.DashboardEducationShown))

            verify(rxSharedPreference).set(eq("key_should_show_home_dashboard_education"), eq(false), any())
        }
    }
}
