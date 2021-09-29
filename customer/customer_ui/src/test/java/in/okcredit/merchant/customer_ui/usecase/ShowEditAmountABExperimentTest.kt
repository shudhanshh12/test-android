package `in`.okcredit.merchant.customer_ui.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class ShowEditAmountABExperimentTest {
    private val ab: AbRepository = mock()
    private val showEditAmountABExperiment = ShowEditAmountABExperiment(ab)

    @Test
    fun `ShowEditAmountABExperiment return false`() {
        whenever(ab.isExperimentEnabled("postlogin_android-all-show_edit_amount_education")).thenReturn(
            Observable.just(
                false
            )
        )

        val testObserver = showEditAmountABExperiment.execute().test()

        testObserver.assertValues(false)

        testObserver.dispose()
    }

    @Test
    fun `ShowEditAmountABExperiment return true and variant is v1`() {
        whenever(ab.isExperimentEnabled("postlogin_android-all-show_edit_amount_education")).thenReturn(
            Observable.just(
                true
            )
        )

        whenever(ab.getExperimentVariant("postlogin_android-all-show_edit_amount_education")).thenReturn(
            Observable.just(
                "v1"
            )
        )

        val testObserver = showEditAmountABExperiment.execute().test()

        testObserver.assertValues(false)

        testObserver.dispose()
    }

    @Test
    fun `ShowEditAmountABExperiment return true and variant is v2`() {
        whenever(ab.isExperimentEnabled("postlogin_android-all-show_edit_amount_education")).thenReturn(
            Observable.just(
                true
            )
        )

        whenever(ab.getExperimentVariant("postlogin_android-all-show_edit_amount_education")).thenReturn(
            Observable.just(
                "v2"
            )
        )

        val testObserver = showEditAmountABExperiment.execute().test()

        testObserver.assertValues(true)

        testObserver.dispose()
    }
}
