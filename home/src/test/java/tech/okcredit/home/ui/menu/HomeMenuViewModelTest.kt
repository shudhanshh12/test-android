package tech.okcredit.home.ui.menu

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.SubmitFeedbackImpl
import `in`.okcredit.collection.contract.GetKycRiskCategory
import `in`.okcredit.collection.contract.GetKycStatus
import `in`.okcredit.collection.contract.IsCollectionActivatedOrOnlinePaymentExist
import `in`.okcredit.collection.contract.ShouldShowCreditCardInfoForKyc
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import `in`.okcredit.merchant.contract.SyncBusinessData
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
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.feature_help.contract.GetSupportNumber
import tech.okcredit.home.ui.sidemenu.usecacse.ShouldShowCallCustomerCare
import tech.okcredit.home.usecase.GetCustomization
import tech.okcredit.home.usecase.GetUnClaimedRewards
import tech.okcredit.home.usecase.ShowFeedback
import tech.okcredit.home.widgets.quick_add_card.usecase.GetQuickAddCardMenuItemVisibility
import tech.okcredit.home.widgets.quick_add_card.usecase.ShowQuickAddCard
import java.util.concurrent.TimeUnit

class HomeMenuViewModelTest {
    private val initialState = HomeMenuContract.HomeMenuState()
    private val getActiveBusiness: GetActiveBusiness = mock()
    private val getCustomization: GetCustomization = mock()
    private val getUnClaimedRewards: GetUnClaimedRewards = mock()
    private val isCollectionActivatedOrOnlinePaymentExist: IsCollectionActivatedOrOnlinePaymentExist = mock()
    private val submitFeedback: SubmitFeedbackImpl = mock()
    private val getQuickAddCardItemVisibility: GetQuickAddCardMenuItemVisibility = mock()
    private val ab: AbRepository = mock()
    private val showFeedback: ShowFeedback = mock()
    private val showQuickAddCard: ShowQuickAddCard = mock()
    private val tracker: Tracker = mock()
    private val shouldShowCreditCardInfoForKyc: ShouldShowCreditCardInfoForKyc = mock()
    private val getKycStatus: GetKycStatus = mock()
    private val getKycRiskCategory: GetKycRiskCategory = mock()
    private val syncBusinessData: SyncBusinessData = mock()
    private val isMultipleAccountEnabled: IsMultipleAccountEnabled = mock()
    private val isKycMenuItemExperimentEnabled: IsKycMenuItemExperimentEnabled = mock()
    private lateinit var testSchedulers: TestScheduler
    private lateinit var viewModel: HomeMenuViewModel
    private val viewEffectObserver = TestObserver<HomeMenuContract.HomeMenuViewEvent>()
    private lateinit var testScheduler: TestScheduler
    private val shouldShowCallCustomerCare = mock<ShouldShowCallCustomerCare>()
    private val getHelpNumber = mock<GetSupportNumber>()

    @Before
    fun setUp() {
        testSchedulers = TestScheduler()
        mockkStatic(Schedulers::class)

        testScheduler = TestScheduler()
        every { Schedulers.computation() } returns testScheduler

        every { Schedulers.newThread() } returns Schedulers.trampoline()
        whenever(getQuickAddCardItemVisibility.execute()).thenReturn(UseCase.wrapObservable(Observable.just(false)))
        whenever(showFeedback.execute()).thenReturn(UseCase.wrapObservable(Observable.just(false)))
        createViewModel()
        viewModel.viewEvent().subscribe(viewEffectObserver)
    }

    fun createViewModel() {
        viewModel = HomeMenuViewModel(
            { initialState },
            { getActiveBusiness },
            { getCustomization },
            { getUnClaimedRewards },
            { submitFeedback },
            { getQuickAddCardItemVisibility },
            { ab },
            { showFeedback },
            { showQuickAddCard },
            { tracker },
            { shouldShowCreditCardInfoForKyc },
            { getKycStatus },
            { getKycRiskCategory },
            { isKycMenuItemExperimentEnabled },
            { syncBusinessData },
            { isCollectionActivatedOrOnlinePaymentExist },
            { isMultipleAccountEnabled },
            { shouldShowCallCustomerCare },
            { getHelpNumber },
        )
    }

    @Test
    fun `test observableForFeedback`() {
        // given
        whenever(submitFeedback.schedule("feedback", 5)).thenReturn(Completable.complete())

        // when
        viewModel.attachIntents(Observable.just(HomeMenuContract.HomeMenuIntent.SubmitFeedback("feedback", 5)))
        val result = viewModel.state().test()
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // then
        Truth.assertThat(
            result.values().last() == initialState
        ).isTrue()
    }

    @Test
    fun `collectionClick Test GotoCollection Merchant Screen`() {
        // given
        whenever(isCollectionActivatedOrOnlinePaymentExist.execute()).thenReturn(Observable.just(true))

        // when
        viewModel.attachIntents(Observable.just(HomeMenuContract.HomeMenuIntent.CollectionClicked))

        // then
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                HomeMenuContract.HomeMenuViewEvent.GoToCollectionScreen
        ).isTrue()
    }

    @Test
    fun `collectionClick Test GotoCollection Screen`() {
        // given
        whenever(isCollectionActivatedOrOnlinePaymentExist.execute()).thenReturn(Observable.just(false))

        // when
        viewModel.attachIntents(Observable.just(HomeMenuContract.HomeMenuIntent.CollectionClicked))

        // then
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                HomeMenuContract.HomeMenuViewEvent.GoToCollectionAdoption
        ).isTrue()
    }

    @Test
    fun showQuickCardTest() {
        // given
        whenever(showQuickAddCard.execute()).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))

        // when
        viewModel.attachIntents(Observable.just(HomeMenuContract.HomeMenuIntent.QuickAddCard))
        val result = viewModel.state().test()
        testScheduler.advanceTimeBy(33, TimeUnit.MILLISECONDS)

        // then
        Truth.assertThat(
            result.values().last() == initialState
        ).isTrue()
    }

    @Test
    fun `move to settingsScreen view event emition`() {
        // when
        viewModel.attachIntents(Observable.just(HomeMenuContract.HomeMenuIntent.SettingsClicked))

        // then
        Truth.assertThat(
            viewEffectObserver.values().last() ==
                HomeMenuContract.HomeMenuViewEvent.GoToSettingsScreen
        ).isTrue()
    }
}
