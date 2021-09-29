package tech.okcredit.feedback

import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.feedback.feedback.FeedbackContract
import tech.okcredit.feedback.feedback.FeedbackViewModel
import tech.okcredit.userSupport.usecses.SubmitFeedback

class FeedbackViewModelTest {
    lateinit var testObserver: TestObserver<FeedbackContract.State>
    private lateinit var feedbackViewModel: FeedbackViewModel
    private val initialState = FeedbackContract.State()
    private val checkNetworkHealth: CheckNetworkHealth = mock()
    private val submitFeedback: SubmitFeedback = mock()

    private fun createViewModel(): FeedbackViewModel {
        return FeedbackViewModel(
            initialState,
            { checkNetworkHealth },
            { submitFeedback },
        )
    }

    @Before
    fun setup() {
        // given
        feedbackViewModel = createViewModel()
        testObserver = feedbackViewModel.state().test()
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `SubmitFeedback event`() {

        // when
        whenever(submitFeedback.schedule("feedback_message", "suggestion")).thenReturn(Completable.complete())
        feedbackViewModel.attachIntents(Observable.just(FeedbackContract.Intent.SubmitFeedback("feedback_message")))

        // then
        Truth.assertThat(testObserver.values().first() == initialState)
    }

    @Test
    fun `load event when network health return success`() {
        // given
        feedbackViewModel = createViewModel()

        // when
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(Observable.just(Result.Success(Unit)))

        feedbackViewModel.attachIntents(Observable.just(FeedbackContract.Intent.Load))

        // then
        Truth.assertThat(testObserver.values().first() == initialState)
        Truth.assertThat(testObserver.values().last() == initialState.copy(networkError = false))
    }

    @Test
    fun `load event when network health return error`() {
        // given
        feedbackViewModel = createViewModel()

        // when
        whenever(checkNetworkHealth.execute(Unit)).thenReturn(Observable.just(Result.Failure(Exception("network error"))))

        feedbackViewModel.attachIntents(Observable.just(FeedbackContract.Intent.Load))

        // then
        Truth.assertThat(testObserver.values().first() == initialState)
    }

    @After
    fun cleanup() {
        testObserver.dispose()
    }
}
