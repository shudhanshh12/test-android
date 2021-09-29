package tech.okcredit.help

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.shared.usecase.UseCase
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.helpHome.HelpHomeContract
import tech.okcredit.help.helpHome.HelpHomeViewModel
import tech.okcredit.help.helpHome.usecase.AddOkcreditNumberToContact
import tech.okcredit.help.helpHome.usecase.SyncHelpData

class HelpHomeViewModelTest {
    lateinit var helpHomeViewModel: HelpHomeViewModel
    private val initialState = HelpHomeContract.State()
    private val getMerchantPreference: GetMerchantPreference = mock()
    private val addOkcreditNumberToContact: AddOkcreditNumberToContact = mock()
    private val getSupportNumber: GetSupportNumber = mock()
    private val syncHelpData: SyncHelpData = mock()
    lateinit var testObserver: TestObserver<HelpHomeContract.State>
    private val viewEffectObserver = TestObserver<HelpHomeContract.ViewEvent>()

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        whenever(getSupportNumber.supportNumber).thenReturn(helpNumber)

        // create Presenter
        createViewModel(initialState)

        // observe state
        testObserver = helpHomeViewModel.state().test()
        helpHomeViewModel.viewEvent().subscribe(viewEffectObserver)
    }

    companion object {
        const val source = "Customer Screen"
        const val helpNumber = "helpNumber"
        const val isWhatsappEnabledForThisUser = true
        val WHATSAPP = PreferenceKey.WHATSAPP
    }

    @After
    fun close() {
        testObserver.dispose()
        viewEffectObserver.dispose()
    }

    private fun createViewModel(initialState: HelpHomeContract.State) {
        helpHomeViewModel = HelpHomeViewModel(
            initialState = initialState,
            source = "Customer screen",
            getMerchantPreference = { getMerchantPreference },
            addOkcreditNumberToContact = { addOkcreditNumberToContact },
            getHelpNumber = { getSupportNumber },
            syncHelpData = { syncHelpData },
        )
    }

    @Test
    fun `when ClickHelp`() {
        // when
        helpHomeViewModel.attachIntents(Observable.just(HelpHomeContract.Intent.ClickHelp))

        // then
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                HelpHomeContract.ViewEvent.GoToHelp(source)
        )
    }

    @Test
    fun `when AboutUsClick`() {
        // when
        helpHomeViewModel.attachIntents(Observable.just(HelpHomeContract.Intent.AboutUsClick))

        // then
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                HelpHomeContract.ViewEvent.GoToAboutUsScreen
        ).isTrue()
    }

    @Test
    fun `when PrivacyClick`() {
        // when
        helpHomeViewModel.attachIntents(Observable.just(HelpHomeContract.Intent.PrivacyClick))

        // then
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                HelpHomeContract.ViewEvent.GoToPrivacyScreen
        ).isTrue()
    }

    @Test
    fun `when OnWhatsAppPermissionCheck`() {
        // given
        whenever(getSupportNumber.supportNumber).thenReturn(helpNumber)
        whenever(getMerchantPreference.execute(WHATSAPP)).thenReturn(Observable.just(isWhatsappEnabledForThisUser.toString()))
        whenever(addOkcreditNumberToContact.execute(helpNumber)).thenReturn(UseCase.wrapCompletable(Completable.complete()))
        helpHomeViewModel.attachIntents(Observable.just(HelpHomeContract.Intent.OnWhatsAppPermissionCheck(true)))

        Truth.assertThat(
            viewEffectObserver.values().last() ==
                HelpHomeContract.ViewEvent.GoToWhatsAppScreen
        )
    }
}
