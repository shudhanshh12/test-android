package tech.okcredit.help

import `in`.okcredit.backend.contract.SubmitFeedback
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import tech.okcredit.help.help_details.HelpDetailsContract
import tech.okcredit.help.help_details.HelpDetailsViewModel
import tech.okcredit.userSupport.SupportRepository
import tech.okcredit.userSupport.data.LikeState

class HelpDetailsViewModelTest {
    private lateinit var helpDetailsViewModel: HelpDetailsViewModel

    private var userSupport: SupportRepository = mock()
    private var submitFeedback: SubmitFeedback = mock()
    private var checkNetworkHealth: CheckNetworkHealth = mock()
    private var context: Context = mock()

    @Before
    fun setUp() {
        Mockito.`when`(checkNetworkHealth.execute(Unit)).thenReturn(UseCase.wrapObservable(Observable.just(Unit)))
    }

    fun createViewModel(initialState: HelpDetailsContract.State) {
        helpDetailsViewModel = HelpDetailsViewModel(
            initialState = initialState,
            userSupport = { userSupport },
            submitFeedback = { submitFeedback },
            source = "Customer Screen",
            helpItemId = "add_customer_HelpItem",
            checkNetworkHealth = checkNetworkHealth
        )
    }

//    @Test
//    fun `when sourceScreen added state source screen should be changed`() {
//        //given
//        val initialState = HelpDetailsContract.State()
//        createViewModel(initialState)
//
//        //when
//        helpDetailsPresenter.attachIntents(Observable.just(HelpDetailsContract.Intent.Load))
//        val result = helpDetailsPresenter.state().test()
//
//        //then
//        Truth.assertThat(result.values().get(0).sourceScreen.equals("Customer screen"))
//
//    }

    @Test
    fun `when Like clicked state isLiked should be true`() {
        // given
        val initialState = HelpDetailsContract.State()
        createViewModel(initialState)

        // when
        helpDetailsViewModel.attachIntents(Observable.just(HelpDetailsContract.Intent.OnLikeClick))
        val result = helpDetailsViewModel.state().test()

        // then
        Truth.assertThat(result.values().get(0).likeState.equals(LikeState.LIKE))
    }

    @Test
    fun `when dislike clicked state isLiked should be false`() {
        // given
        val initialState = HelpDetailsContract.State()
        createViewModel(initialState)

        // when
        helpDetailsViewModel.attachIntents(Observable.just(HelpDetailsContract.Intent.OnDisLikeClick))
        val result = helpDetailsViewModel.state().test()

        // then
        Truth.assertThat(result.values().first().likeState == LikeState.DISLIKE)
    }

    @Test
    fun `when help contact us helper loaded helpItemId enabled`() {
        // given
        val initialState = HelpDetailsContract.State()
        createViewModel(initialState)

        // when
        val helpItem = TestData.helpList.get(0).help_items?.get(0)
        whenever(userSupport.getHelpItem("add_customer_HelpItem"))
            .thenReturn(Observable.just(helpItem))

        helpDetailsViewModel.attachIntents(Observable.just(HelpDetailsContract.Intent.Load))
        val result = helpDetailsViewModel.state().test()

        // then
        result.values().contains(
            HelpDetailsContract.State(helpItemV2 = helpItem)
        )

        result.dispose()
    }

    @Test
    fun `when helpDetailsContract submit feedback`() {
        // given
        val initialState = HelpDetailsContract.State()
        createViewModel(initialState)

        // when
        whenever(submitFeedback.schedule("feedback_message", 5)).thenReturn(Completable.complete())
        val result = helpDetailsViewModel.state().test()

        // then
        result.values().contains(initialState)
    }

    @Test
    fun `when helpDetailsContract submit feedback goAfterAnimation`() {
        // given
        val initialState = HelpDetailsContract.State()
        createViewModel(initialState)

        // when
        whenever(submitFeedback.schedule("feedback_message", 5)).thenReturn(Completable.complete())
        helpDetailsViewModel.attachIntents(Observable.just(HelpDetailsContract.Intent.SubmitFeedback("feedback_message", 5)))
        val result = helpDetailsViewModel.state().test()

        // then
        result.values().contains(initialState)
    }
}
