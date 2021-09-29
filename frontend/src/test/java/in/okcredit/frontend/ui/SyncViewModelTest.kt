package `in`.okcredit.frontend.ui

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.ui.sync.SyncContract
import `in`.okcredit.frontend.ui.sync.SyncViewModel
import `in`.okcredit.frontend.usecase.LoginDataSyncerImpl
import `in`.okcredit.frontend.usecase.language_experiment.ShouldShowSelectBusinessFragment
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.UseCase
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class SyncViewModelTest {
    private val initialState = SyncContract.State()
    private val syncAuthScope: LoginDataSyncerImpl = mock()
    private val tracker: Tracker = mock()
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val shouldShowSelectBusinessFragment: ShouldShowSelectBusinessFragment = mock()
    private lateinit var syncViewModel: SyncViewModel
    private lateinit var testScheduler: TestScheduler

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        createViewModel()
    }

    private fun createViewModel() {
        syncViewModel = SyncViewModel(
            initialState,
            { syncAuthScope },
            { tracker },
            { checkNetworkHealth },
            { getActiveBusinessId },
            { shouldShowSelectBusinessFragment }
        )
    }

    @Test
    fun `checkNetworkHealth test`() {
        // given
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))

        // when
        syncViewModel.attachIntents(Observable.just(SyncContract.Intent.Load))
        val result = syncViewModel.state().test()
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // then
        Truth.assertThat(
            result.values().last().networkError
        ).isFalse()
    }

    @Test
    fun `on checkNetwork return error`() {
        // given
        val mockError = Throwable()
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.error(mockError)))

        // when
        syncViewModel.attachIntents(Observable.just(SyncContract.Intent.Load))
        val result = syncViewModel.state().test()
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // then
        Truth.assertThat(
            result.values().last() == initialState
        ).isTrue()
    }

    @Test
    fun `syncContract Retry Test`() {
        syncViewModel.attachIntents(Observable.just(SyncContract.Intent.Retry))
        val result = syncViewModel.state().test()
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // then
        Truth.assertThat(
            result.values().last().error
        ).isFalse()
    }
}
