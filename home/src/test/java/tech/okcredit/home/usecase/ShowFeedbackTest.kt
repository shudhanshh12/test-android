package tech.okcredit.home.usecase

import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class ShowFeedbackTest {
    private val ab: AbRepository = mock()

    private val showFeedback = tech.okcredit.home.usecase.ShowFeedback(
        Lazy { ab }
    )
    val EXPERIMENT_NAME = "ui_experiment-all-feedback"
    val FEEDBACK_VERIENT = "feedback"

    @Test
    fun `Usecase should return true when ab varient is feedback`() {
        whenever(ab.getExperimentVariant(EXPERIMENT_NAME)).thenReturn(Observable.just(FEEDBACK_VERIENT))

        // when
        val testObserver =
            showFeedback.execute().subscribeOn(Schedulers.trampoline()).test()

        // Then
        testObserver.assertValues(
            Result.Progress(),
            Result.Success(true)
        )

        testObserver.dispose()
    }
}
