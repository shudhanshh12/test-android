package tech.okcredit.help

import `in`.okcredit.backend.contract.GetMerchantPreference
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import com.google.common.truth.Truth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.help.helpHome.usecase.AddOkcreditNumberToContact
import tech.okcredit.help.help_main.HelpContract
import tech.okcredit.help.help_main.HelpViewModel
import tech.okcredit.userSupport.SupportRepository

internal class HelpMainViewModelTest {
    lateinit var helpViewModel: HelpViewModel

    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val userSupport: SupportRepository = mock()
    private val addOkcreditNumberToContact: AddOkcreditNumberToContact = mock()
    private val getMerchantPreference: GetMerchantPreference = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val getSupportNumber: GetSupportNumber = mock()
    private val initialState = HelpContract.State()
    lateinit var testObserver: TestObserver<HelpContract.State>
    private val viewEffectObserver = TestObserver<HelpContract.ViewEvent>()
    private val helpNumber = "helpNumber"

    companion object {
        val filterHelpIds = listOf("customer_related_questions", "transaction")
    }

    private fun createViewModel(initialState: HelpContract.State) {
        helpViewModel = HelpViewModel(
            initialState = initialState,
            userSupport = { userSupport },
            filterHelpIds = filterHelpIds,
            source = "Customer Screen",
            contextualHelp = null,
            checkNetworkHealth = checkNetworkHealth,
            addOkCreditNumberToContact = { addOkcreditNumberToContact },
            getMerchantPreference = { getMerchantPreference },
            getHelpNumber = { getSupportNumber }
        )
    }

    @Before
    fun setUp() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        whenever(getSupportNumber.supportNumber).thenReturn(helpNumber)

        // create Presenter
        createViewModel(initialState)

        // observe state
        testObserver = helpViewModel.state().test()
        helpViewModel.viewEvent().subscribe(viewEffectObserver)
    }

//    @Test
//    fun `when source screen added state source screen should be changed`(){
//        //given
//        val initialState = HelpContract.State()
//        createPresneter(initialState)
//
//        //when
//        helpPresenter.attachIntents(Observable.just(HelpContract.Intent.Load))
//        val result = helpPresenter.state().test()
//
//        //then
//        Truth.assertThat(result.values().get(0).sourceScreen.equals("Customer screen"))
//
//    }

//    @Test
//    fun `when loaded assign the contextualHelpId in state`(){
//        //given
//        val initialState = HelpContract.State()
//        createPresneter(initialState)
//
//        //when
//        helpPresenter.attachIntents(Observable.just(HelpContract.Intent.Load))
//        val result = helpPresenter.state().test()
//
//        //then
//        Truth.assertThat(result.values().get(0).sourceScreen.equals("customer_related_questions"))
//
//    }

//    @Test
//    fun `when load get the help item from usecase`() {
//        //given
//        val initialState = HelpContract.State()
//        createPresneter(initialState)
//
//        //when
//        whenever(userSupport.getHelp()).thenReturn(Observable.just(helpList))
//        helpPresenter.attachIntents(Observable.just(HelpContract.Intent.Load))
// //        val result = helpPresenter.state().test()
//
//        //then
// //        Truth.assertThat(result.values().get(0).help?.get(0)?.title?.equals("Add a new customer_Help"))
//        Truth.assertThat(testObserver.values().first().help?.get(0)?.title?.equals("Add a new customer_Help"))
//
//    }

    @Test
    fun `when main_item click it should set as expand id`() {
        // given
        val initialState = HelpContract.State()
        createViewModel(initialState)

        // when
        helpViewModel.attachIntents(
            Observable.just(
                HelpContract.Intent.MainItemClick(
                    "customer_related_questions",
                    true
                )
            )
        )
        val result = helpViewModel.state().test()

        // then
        Truth.assertThat(result.values().get(0).expandedId.equals("customer_related_questions"))
    }

    @Test
    fun `when section_item_click  it should move to help item screen`() {
        // given
        val initialState = HelpContract.State()
        createViewModel(initialState)

        // when
        helpViewModel.attachIntents(Observable.just(HelpContract.Intent.OnSectionItemClick("customer_related_questions")))
        val result = helpViewModel.state().test()

        // then
        Truth.assertThat(result.values().get(0) == initialState)
    }

    @Test
    fun `when ShowAlert  it should give alert`() {
        // given
        val initialState = HelpContract.State()
        createViewModel(initialState)

        // when
        helpViewModel.attachIntents(Observable.just(HelpContract.Intent.ShowAlert("Test String")))
        val result = helpViewModel.state().test()

        // then
        Truth.assertThat(result.values().get(0).alertMessage.equals("Test String"))
    }
}
