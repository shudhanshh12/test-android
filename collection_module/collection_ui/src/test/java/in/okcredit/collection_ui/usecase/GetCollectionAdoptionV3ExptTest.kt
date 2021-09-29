package `in`.okcredit.collection_ui.usecase

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Assert.*
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class GetCollectionAdoptionV3ExptTest {

    private val abRepository: AbRepository = mock()

    private val getCollectionAdoptionV3Expt = GetCollectionAdoptionV3Expt({ abRepository })

    private val expt = "postlogin_android-all-collection_adoption_v3"

    @Test
    fun verifyExpName() {
        Truth.assertThat(GetCollectionAdoptionV3Expt.EXPT_NAME).isEqualTo(expt)
    }

    @Test
    fun `execute() should return true`() {
        val variant = "v3"
        whenever(abRepository.isExperimentEnabled(expt)).thenReturn(Observable.just(true))
        whenever(abRepository.getExperimentVariant(expt)).thenReturn(Observable.just(variant))

        val testObserver = getCollectionAdoptionV3Expt.execute().test()

        testObserver.assertValues(true)
        testObserver.dispose()
    }

    @Test
    fun `execute() should return false if variant v2`() {
        val variant = "v2"
        whenever(abRepository.isExperimentEnabled(expt)).thenReturn(Observable.just(true))
        whenever(abRepository.getExperimentVariant(expt)).thenReturn(Observable.just(variant))

        val testObserver = getCollectionAdoptionV3Expt.execute().test()

        testObserver.assertValues(false)
        testObserver.dispose()
    }
}
