package tech.okcredit.applock.changeSecurityPin

import `in`.okcredit.backend.contract.Authenticate
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.onboarding.enterotp.usecase.RequestOtp
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.OtpToken
import tech.okcredit.android.auth.usecases.IsPasswordSet
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.applock.changePin.ChangeSecurityPinContract
import tech.okcredit.applock.changePin.ChangeSecurityPinViewModel

class ChangeSecurityPinViewModelTest {
    private val getActiveBusiness: GetActiveBusiness = mock()
    private val requestOtp: RequestOtp = mock()
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val authenticate: Authenticate = mock()
    private val isPasswordSet: IsPasswordSet = mock()
    private val viewEffectObserver = TestObserver<ChangeSecurityPinContract.ViewEvent>()
    val initialState = ChangeSecurityPinContract.State()

    lateinit var viewModel: ChangeSecurityPinViewModel

    private fun createViewModel(initialState: ChangeSecurityPinContract.State): ChangeSecurityPinViewModel {
        return ChangeSecurityPinViewModel(
            { initialState },
            source = "source",
            entry = "entry",
            { getActiveBusiness },
            { requestOtp },
            { checkNetworkHealth },
            { authenticate },
            { isPasswordSet }
        )
    }

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        viewModel = createViewModel(initialState)
        viewModel.viewEvent().subscribe(viewEffectObserver)
    }

    @After
    fun close() {
        viewEffectObserver.dispose()
    }

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")
        val mobile = "mobile"
        val merchant = Business(
            id = "merchant_id",
            name = " merchant_name",
            mobile = mobile,
            createdAt = dt
        )
        val otpToken = OtpToken(id = "id")
        val otpString = "1234"
    }

    @Test
    fun `get mobile number from getActivemerchant`() {
        // when
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(merchant))

        val stateObserver = TestObserver<ChangeSecurityPinContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                mobile = mobile
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `test isPassword set`() {
        // given
        whenever(isPasswordSet.execute()).thenReturn(Single.just(true))

        // when
        val stateObserver = TestObserver<ChangeSecurityPinContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                isUpdatePassword = true
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `setSource`() {
        val stateObserver = TestObserver<ChangeSecurityPinContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                source = "source"
            )
        )

        stateObserver.dispose()
    }

    @Test
    fun `sendOTP test`() {
        // given
        whenever(checkNetworkHealth.isConnectedToInternet()).thenReturn(true)
        whenever(requestOtp.execute(mobile)).thenReturn(Single.just(otpToken))

        // when
        viewModel.attachIntents(Observable.just(ChangeSecurityPinContract.Intent.VerifyOtp(otpString)))

        val stateObserver = TestObserver<ChangeSecurityPinContract.State>()
        viewModel.state().subscribe(stateObserver)

        Truth.assertThat(stateObserver.values().first() == initialState)
        Truth.assertThat(
            stateObserver.values().last() == initialState.copy(
                otpSent = true,
                incorrectOtp = false,
                verificationInProgress = false,
                errorMessage = null
            )
        )

        stateObserver.dispose()
    }
}
