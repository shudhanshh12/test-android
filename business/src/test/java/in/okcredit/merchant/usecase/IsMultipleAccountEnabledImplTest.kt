package `in`.okcredit.merchant.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Assert.*
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class IsMultipleAccountEnabledImplTest {
    private val abRepository: AbRepository = mock()

    private val isMultipleAccountFeatureEnabled = IsMultipleAccountEnabledImpl { abRepository }

    @Test
    fun `execute should return true`() {
        val feature = "multiple_accounts"
        assertEquals(feature, IsMultipleAccountEnabledImpl.FEATURE)
        whenever(abRepository.isFeatureEnabled(feature)).thenReturn(Observable.just(true))

        val testObserver = isMultipleAccountFeatureEnabled.execute().test()

        testObserver.assertValue(true)
        testObserver.dispose()
    }
}
