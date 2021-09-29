package tech.okcredit.applock.enterpin

import `in`.okcredit.shared.usecase.CheckNetworkHealth
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.usecases.VerifyPassword
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.applock.enterPin.EnterPinContract
import tech.okcredit.applock.enterPin.EnterPinViewModel
import tech.okcredit.applock.usecase.GetMerchantFingerprintPreference
import tech.okcredit.contract.MerchantPrefSyncStatus

class EnterPinViewModelTest {
    lateinit var enterPinViewModel: EnterPinViewModel
    private val initialState = EnterPinContract.State()
    private val verifyPassword: VerifyPassword = mock()
    private val getMerchantFingerprintPreference: GetMerchantFingerprintPreference = mock()
    private val merchantPrefSyncStatus: MerchantPrefSyncStatus = mock()
    private val viewEffectObserver = TestObserver<EnterPinContract.ViewEvent>()
    private val checkNetworkHealth: CheckNetworkHealth = mock()

    companion object {
        val pin = "pin"
        const val FINGERPRINT_ENABLED = "FINGERPRINT_ENABLE"
    }

    @After
    fun close() {
        viewEffectObserver.dispose()
    }

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        enterPinViewModel = createViewModel(initialState)
        enterPinViewModel.viewEvent().subscribe(viewEffectObserver)
    }

    @Test
    fun setPinTest() {
        // when
        enterPinViewModel.attachIntents(Observable.just(EnterPinContract.Intent.VerifyPin(pin)))

        val stateObserver = TestObserver<EnterPinContract.State>()
        enterPinViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                pin = pin
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun verifyPinTest() {
        // when
        whenever(verifyPassword.execute(pin)).thenReturn(Completable.complete())
        enterPinViewModel.attachIntents(Observable.just(EnterPinContract.Intent.VerifyPin(pin)))

        val stateObserver = TestObserver<EnterPinContract.State>()
        enterPinViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState
        )

        stateObserver.dispose()
    }

    @Test
    fun sourceTest() {
        enterPinViewModel.attachIntents(Observable.just(EnterPinContract.Intent.Load))

        val stateObserver = TestObserver<EnterPinContract.State>()
        enterPinViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last().source == "source"
        )

        stateObserver.dispose()
    }

    @Test
    fun checkFingerPrintEnableInDevice() {
        whenever(merchantPrefSyncStatus.checkFingerPrintAvailability()).thenReturn(Observable.just(true))
        enterPinViewModel.attachIntents(Observable.just(EnterPinContract.Intent.Load))

        val stateObserver = TestObserver<EnterPinContract.State>()
        enterPinViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last().isFingerprintEnrolledInDevice
        )

        stateObserver.dispose()
    }

    @Test
    fun checkFingerPrintNotEnableInDevice() {
        whenever(merchantPrefSyncStatus.checkFingerPrintAvailability()).thenReturn(Observable.just(false))
        enterPinViewModel.attachIntents(Observable.just(EnterPinContract.Intent.Load))

        val stateObserver = TestObserver<EnterPinContract.State>()
        enterPinViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last().isFingerprintEnrolledInDevice.not()
        )

        stateObserver.dispose()
    }

    @Test
    fun checkFingerPrintEnableInApp() {
        whenever(getMerchantFingerprintPreference.execute()).thenReturn(Observable.just(true))
        enterPinViewModel.attachIntents(Observable.just(EnterPinContract.Intent.Load))

        val stateObserver = TestObserver<EnterPinContract.State>()
        enterPinViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last().isFingerPrintEnabled
        )

        stateObserver.dispose()
    }

    @Test
    fun checkFingerPrintNotEnableInApp() {
        whenever(getMerchantFingerprintPreference.execute()).thenReturn(Observable.just(false))
        enterPinViewModel.attachIntents(Observable.just(EnterPinContract.Intent.Load))

        val stateObserver = TestObserver<EnterPinContract.State>()
        enterPinViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last().isFingerPrintEnabled.not()
        )

        stateObserver.dispose()
    }

    private fun createViewModel(initialState: EnterPinContract.State): EnterPinViewModel {
        return EnterPinViewModel(
            initialState = Lazy { initialState },
            source = "source",
            verifyPassword = Lazy { verifyPassword },
            getMerchantFingerprintPreference = Lazy { getMerchantFingerprintPreference },
            merchantPrefSyncStatus = Lazy { merchantPrefSyncStatus },
            checkInternetAvailable = Lazy { checkNetworkHealth }
        )
    }
}
