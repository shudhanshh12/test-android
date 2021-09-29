package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.customer_ui.usecase.CanShowCollectWithGPay.Companion.CONTROL
import `in`.okcredit.merchant.customer_ui.usecase.CanShowCollectWithGPay.Companion.EXPT
import `in`.okcredit.merchant.customer_ui.usecase.CanShowCollectWithGPay.Companion.TEST
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Assert.*
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class CanShowCollectWithGPayTest {

    private val ab: AbRepository = mock()

    private val canShowCollectWithGPay = CanShowCollectWithGPay { ab }

    @Test
    fun verifyExperimentName() {
        assertEquals("postlogin_android-all-gpay_experiment", EXPT)
    }

    @Test
    fun `execute() should return true for TEST variant`() {
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(TEST))

        val testObserver = canShowCollectWithGPay.execute().test()

        testObserver.assertValue(true)
        testObserver.dispose()
    }

    @Test
    fun `execute() should return false for CONTROL variant`() {
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(CONTROL))

        val testObserver = canShowCollectWithGPay.execute().test()

        testObserver.assertValue(false)
        testObserver.dispose()
    }

    @Test
    fun `execute() should return false if expt not enabled`() {
        whenever(ab.isExperimentEnabled(EXPT)).thenReturn(Observable.just(false))
        whenever(ab.getExperimentVariant(EXPT)).thenReturn(Observable.just(CONTROL))

        val testObserver = canShowCollectWithGPay.execute().test()

        testObserver.assertValue(false)
        testObserver.dispose()
    }
}
