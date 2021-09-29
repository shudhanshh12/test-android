package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase.IsRoboflowFeatureEnabled.Companion.FEATURE_NAME
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class IsRoboflowFeatureEnabledTest {
    private val ab: AbRepository = mock()
    private val isRoboflowFeatureEnabled = IsRoboflowFeatureEnabled { ab }

    @Test
    fun `When feature is enabled then return true`() {
        whenever(ab.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(true))

        val result = isRoboflowFeatureEnabled.execute().subscribeOn(Schedulers.trampoline()).test()

        result.assertValue(true)
    }

    @Test
    fun `When feature is disabled then return false`() {
        whenever(ab.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(false))

        val result = isRoboflowFeatureEnabled.execute().subscribeOn(Schedulers.trampoline()).test()

        result.assertValue(false)
    }
}
