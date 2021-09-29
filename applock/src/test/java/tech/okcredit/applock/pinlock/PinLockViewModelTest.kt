package tech.okcredit.applock.pinlock

import `in`.okcredit.shared.usecase.UseCase
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.android.auth.usecases.ResetPassword
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.applock.pinLock.PinLockContract
import tech.okcredit.applock.pinLock.PinLockViewModel
import tech.okcredit.applock.pinLock.usecase.UpdatePinPrefStatus

class PinLockViewModelTest {
    lateinit var pinLockViewModel: PinLockViewModel
    private val isPasswordSet: IsPasswordSet = mock()
    private val resetPassword: ResetPassword = mock()
    private val initialState = PinLockContract.State()
    private val updatePinPrefStatus: UpdatePinPrefStatus = mock()
    private val viewEffectObserver = TestObserver<PinLockContract.ViewEvent>()

    private fun createViewModel(initialState: PinLockContract.State): PinLockViewModel {
        return PinLockViewModel(
            initialState = { initialState },
            source = "source",
            entry = "entry",
            isPasswordSet = { isPasswordSet },
            resetPassword = { resetPassword },
            updatePinPrefStatus = { updatePinPrefStatus }
        )
    }

    companion object {
        val pin = "pin"
        val Incorrectpin = "pin"
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
        pinLockViewModel = createViewModel(initialState)
        pinLockViewModel.viewEvent().subscribe(viewEffectObserver)
    }

    @Test
    fun `is passwordset test`() {
        whenever(isPasswordSet.execute()).thenReturn(Single.just(true))
        pinLockViewModel.attachIntents(Observable.just(PinLockContract.Intent.Load))

        val stateObserver = TestObserver<PinLockContract.State>()
        pinLockViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                isUpdatePin = true
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `set pin add the pin value`() {
        pinLockViewModel.attachIntents(Observable.just(PinLockContract.Intent.SetPin(pin)))

        val stateObserver = TestObserver<PinLockContract.State>()
        pinLockViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                pinValue = pin
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `verifyPin add the pin value`() {
        pinLockViewModel.attachIntents(Observable.just(PinLockContract.Intent.ConfirmPin(pin, pin)))

        val stateObserver = TestObserver<PinLockContract.State>()
        pinLockViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState
        )

        stateObserver.dispose()
    }

    @Test
    fun `verifyPin add the InCorrectpin value`() {
        pinLockViewModel.attachIntents(Observable.just(PinLockContract.Intent.ConfirmPin(pin, Incorrectpin)))

        val stateObserver = TestObserver<PinLockContract.State>()
        pinLockViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                isIncorrectPin = true
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `verifyPinApiCall test`() {
        // given
        whenever(resetPassword.execute(pin)).thenReturn(Completable.complete())
        pinLockViewModel.attachIntents(Observable.just(PinLockContract.Intent.ConfirmPin(pin, pin)))

        val stateObserver = TestObserver<PinLockContract.State>()
        pinLockViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState
        )

        stateObserver.dispose()
    }

    @Test
    fun `updateMerchantPreference test`() {
        // given
        whenever(updatePinPrefStatus.execute(true)).thenReturn(UseCase.wrapCompletable(Completable.complete()))
        pinLockViewModel.attachIntents(Observable.just(PinLockContract.Intent.FourDigitPinUpdated))

        val stateObserver = TestObserver<PinLockContract.State>()
        pinLockViewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState
        )

        stateObserver.dispose()
    }
}
