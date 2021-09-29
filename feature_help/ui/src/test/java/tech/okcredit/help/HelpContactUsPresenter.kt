package tech.okcredit.help

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.helpcontactus.HelpContactUsContract
import tech.okcredit.help.helpcontactus.HelpContactUsViewModel

class HelpContactUsViewModelTest {

    private lateinit var helpContactUsViewModel: HelpContactUsViewModel

    private val getMerchantPreference: GetMerchantPreference = mock()
    private val ab: AbRepository = mock()
    private val context: Context = mock()
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val navigator: HelpContactUsContract.Navigator = mock()
    private val getSupportNumber: GetSupportNumber = mock()

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        whenever(getSupportNumber.supportNumber).thenReturn("helpNumber")
        Mockito.`when`(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))
    }

    fun createViewModel(initialState: HelpContactUsContract.State) {
        helpContactUsViewModel = HelpContactUsViewModel(
            initialState = initialState,
            getMerchantPreference = getMerchantPreference,
            ab = ab,
            source = "Customer screen",
            context = context,
            checkNetworkHealth = checkNetworkHealth,
            navigator = navigator,
            getHelpNumber = { getSupportNumber }
        )
    }

//    @Test
//    fun `when loaded source screen`() {
//        //given
//        val initialState = HelpContactUsContract.State()
//        createViewModel(initialState)
//
//        //when
//        helpContactUsPresenter.attachIntents(Observable.just(HelpContactUsContract.Intent.Load))
//        val result = helpContactUsPresenter.state().test()
//
//        //then
//        Truth.assertThat(result.values().get(0).sourceScreen.equals("Customer screen"))
//
//    }

    @Test
    fun `when email Us intent`() {
        // given
        val initialState = HelpContactUsContract.State()
        createViewModel(initialState)

        // when
        helpContactUsViewModel.attachIntents(Observable.just(HelpContactUsContract.Intent.EmailUs))
        val result = helpContactUsViewModel.state().test()

        // then
        verify(navigator).onEmailClicked()
    }

    @Test
    fun `when help contact us helper loaded`() {
        // given
        val initialState = HelpContactUsContract.State()
        createViewModel(initialState)

        // when
        whenever(ab.isFeatureEnabled(HelpContactUsViewModel.HELP_FEATURE))
            .thenReturn(Observable.just(true))

        helpContactUsViewModel.attachIntents(Observable.just(HelpContactUsContract.Intent.Load))
        val result = helpContactUsViewModel.state().test()

        // then
        result.values().contains(
            HelpContactUsContract.State(isManualChatEnabled = true)
        )

        result.dispose()
    }

    @Test
    fun `when help contact us helper loaded manualchat enabled`() {
        // given
        val initialState = HelpContactUsContract.State()
        createViewModel(initialState)

        // when
        whenever(ab.isFeatureEnabled(HelpContactUsViewModel.HELP_FEATURE))
            .thenReturn(Observable.just(false))

        helpContactUsViewModel.attachIntents(Observable.just(HelpContactUsContract.Intent.Load))
        val result = helpContactUsViewModel.state().test()

        // then
        result.values().contains(
            HelpContactUsContract.State(isManualChatEnabled = false)
        )

        result.dispose()
    }
}
