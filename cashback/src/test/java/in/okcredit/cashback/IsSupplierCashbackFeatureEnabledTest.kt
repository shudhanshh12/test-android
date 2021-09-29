package `in`.okcredit.cashback

import `in`.okcredit.cashback.usecase.IsSupplierCashbackFeatureEnabledImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class IsSupplierCashbackFeatureEnabledTest {
    private val ab: AbRepository = mock()
    private val isSupplierCashbackFeatureEnabled = IsSupplierCashbackFeatureEnabledImpl { ab }

    companion object {
        private const val FEATURE_NAME = "supplier_cashback"
    }

    @Test
    fun `When feature is enabled then return true`() {
        whenever(ab.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(true))

        val result = isSupplierCashbackFeatureEnabled.execute().subscribeOn(Schedulers.trampoline()).test()

        result.assertValue(true)
    }

    @Test
    fun `When pay online feature is disabled then return false`() {
        whenever(ab.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(false))

        val result = isSupplierCashbackFeatureEnabled.execute().subscribeOn(Schedulers.trampoline()).test()

        result.assertValue(false)
    }
}
