package tech.okcredit.home.ui.sidemenu.usecacse

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class ShouldShowCallCustomerCareTest {

    private val abRepository = mock<AbRepository>()

    private val shouldShowCallCustomerCare = ShouldShowCallCustomerCare { abRepository }

    @Test
    fun `should return false when repository method returns false`() {
        whenever(abRepository.isFeatureEnabled("phone_support")).thenReturn(Observable.just(false))

        val testObserver = shouldShowCallCustomerCare.execute().test()
        testObserver.assertValues(false)
    }

    @Test
    fun `should return true when repository method returns true`() {
        whenever(abRepository.isFeatureEnabled("phone_support")).thenReturn(Observable.just(true))

        val testObserver = shouldShowCallCustomerCare.execute().test()
        testObserver.assertValues(true)
    }
}
