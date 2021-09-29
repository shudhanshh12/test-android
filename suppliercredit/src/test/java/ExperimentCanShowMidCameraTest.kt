import `in`.okcredit.merchant.suppliercredit.use_case.ExperimentCanShowMidCamera
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class ExperimentCanShowMidCameraTest {
    private val ab: AbRepository = mock()
    private var experimentCanShowMidCamera = ExperimentCanShowMidCamera(ab)

    companion object {
        private const val EXPERIMENT_NAME = "postlogin_android-all-add_image_icon_tx_screen"
    }

    @Test
    fun `execute test if experiment enabled`() {
        // given
        whenever(ab.isExperimentEnabled(EXPERIMENT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPERIMENT_NAME)).thenReturn(Observable.just("midCamera"))

        // when
        val result = experimentCanShowMidCamera.execute(Unit).test()

        // then
        result.assertValueAt(1, Result.Success(true))
    }

    @Test
    fun `execute test if experiment not enabled`() {
        // given
        whenever(ab.isExperimentEnabled(EXPERIMENT_NAME)).thenReturn(Observable.just(true))
        whenever(ab.getExperimentVariant(EXPERIMENT_NAME)).thenReturn(Observable.just("defaultCamera"))

        // when
        val result = experimentCanShowMidCamera.execute(Unit).test()

        // then
        result.assertValueAt(1, Result.Success(false))
    }
}
