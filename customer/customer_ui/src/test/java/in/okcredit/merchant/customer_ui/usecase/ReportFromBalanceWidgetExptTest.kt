package `in`.okcredit.merchant.customer_ui.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Assert.*
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class ReportFromBalanceWidgetExptTest {

    companion object {
        const val EXPT = "postlogin_android-all-report_from_balance_widget"

        const val TEST = "TEST"
        const val CONTROL = "CONTROL"
    }

    private val ab: AbRepository = mock()

    private val reportFromBalanceWidgetExpt = ReportFromBalanceWidgetExpt { ab }

    @Test
    fun verifyExperimentName() {
        assertEquals("postlogin_android-all-report_from_balance_widget", EXPT)
    }

    @Test
    fun `execute() should return true for TEST variant`() {
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(TEST))

        val testObserver = reportFromBalanceWidgetExpt.execute().test()

        testObserver.assertValue(true)
        testObserver.dispose()
    }

    @Test
    fun `execute() should return false for CONTROL variant`() {
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(CONTROL))

        val testObserver = reportFromBalanceWidgetExpt.execute().test()

        testObserver.assertValue(false)
        testObserver.dispose()
    }

    @Test
    fun `execute() should return no value if expt not enabled`() {
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(false))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(CONTROL))

        val testObserver = reportFromBalanceWidgetExpt.execute().test()

        testObserver.assertValueCount(0)
        testObserver.dispose()
    }
}
