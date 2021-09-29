package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.Features
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Assert.*
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class GetCanShowSupplierTabVideoTest {

    private val ab: AbRepository = mock()

    private val getCanShowSupplierTabVideo = GetCanShowSupplierTabVideo(ab)

    @Test
    fun `execute return false when feature is disabled`() {
        whenever(ab.isFeatureEnabled((Features.HOME_SUPPLIER_TAB_VIDEO))).thenReturn(
            Observable.just(false)
        )

        val testObserver = getCanShowSupplierTabVideo.execute(Unit).map {
            when (it) {
                is Result.Success -> it.value
                else -> false
            }
        }.test()
        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, false)
        testObserver.assertValueAt(1, false)
        testObserver.dispose()
    }
}
