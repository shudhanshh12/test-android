package `in`.okcredit.frontend.ui.confirmPhoneChange

import `in`.okcredit.frontend.FrontendTestData
import `in`.okcredit.frontend.ui.confirm_phone_change.ConfirmNumberChangeContract
import `in`.okcredit.frontend.ui.confirm_phone_change.ConfirmNumberChangeViewModel
import `in`.okcredit.merchant.usecase.GetActiveBusinessImpl
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class ConfirmPhonechangeViewModelTest {
    private val initialState = ConfirmNumberChangeContract.State()
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val getActiveMerchant: GetActiveBusinessImpl = mock()
    private val navigator: ConfirmNumberChangeContract.Navigator = mock()
    private lateinit var confirmNumberChangeViewModel: ConfirmNumberChangeViewModel

    @Before
    fun setUp() {
        createViewModel()

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    fun createViewModel() {
        confirmNumberChangeViewModel = ConfirmNumberChangeViewModel(
            initialState,
            tempNewNumber = "tempNewNumber",
            { checkNetworkHealth },
            getActiveMerchant,
            navigator
        )
    }

    @Test
    fun `checkNetworkHealth test`() {
        // given
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))
        whenever(getActiveMerchant.execute()).thenReturn(Observable.just(FrontendTestData.MERCHANT))

        // when
        confirmNumberChangeViewModel.attachIntents(Observable.just(ConfirmNumberChangeContract.Intent.Load))
        // TODO have to be removed and use testSchduler and advanceBy
        Thread.sleep(100)
        val result = confirmNumberChangeViewModel.state().test()

        // then
        result.assertValue {
            it.networkError.not()
        }
    }

    @Test
    fun `test active merchant`() {
        // given
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))
        whenever(getActiveMerchant.execute()).thenReturn(Observable.just(FrontendTestData.MERCHANT))

        // when
        confirmNumberChangeViewModel.attachIntents(Observable.just(ConfirmNumberChangeContract.Intent.Load))
        Thread.sleep(100) // TODO have to be removed and use testSchduler and advanceBy
        val result = confirmNumberChangeViewModel.state().test()

        // then
        result.assertValue {
            it.business == FrontendTestData.MERCHANT && it.tempNewNumber == "tempNewNumber"
        }
    }

    @Test
    fun `test verify and change intent`() {

        // when
        confirmNumberChangeViewModel.attachIntents(Observable.just(ConfirmNumberChangeContract.Intent.VerfiyAndChange))
        Thread.sleep(100) // TODO have to be removed and use testSchduler and advanceBy

        // then
        verify(navigator).goToOTPVerificationScreen("tempNewNumber")
    }

    @Test
    fun `checkNetworkHealth test fails`() {
        // given
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(Observable.just(Result.Failure(Exception("network error"))))

        // when
        confirmNumberChangeViewModel.attachIntents(Observable.just(ConfirmNumberChangeContract.Intent.Load))
        Thread.sleep(100) // TODO have to be removed and use testSchduler and advanceBy
        val result = confirmNumberChangeViewModel.state().test()

        // then
        result.assertValue {
            it == ConfirmNumberChangeContract.State()
        }
    }
}
