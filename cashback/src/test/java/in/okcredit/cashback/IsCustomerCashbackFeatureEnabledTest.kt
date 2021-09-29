package `in`.okcredit.cashback

import `in`.okcredit.backend.contract.Features
import `in`.okcredit.cashback.usecase.IsCustomerCashbackFeatureEnabledImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class IsCustomerCashbackFeatureEnabledTest {
    private val ab: AbRepository = mock()
    private val isCustomerCashbackFeatureEnabled = IsCustomerCashbackFeatureEnabledImpl { ab }
    companion object {
        private const val FEATURE_NAME = "customer_cashback"
    }

    @Test
    fun `When feature is enabled then return true`() {
        whenever(ab.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(true))
        whenever(ab.isFeatureEnabled(Features.CUSTOMER_ONLINE_PAYMENT)).thenReturn(Observable.just(true))

        val result = isCustomerCashbackFeatureEnabled.execute().subscribeOn(Schedulers.trampoline()).test()

        result.assertValue(true)
    }

    @Test
    fun `When pay online feature is disabled then return false`() {
        whenever(ab.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(true))
        whenever(ab.isFeatureEnabled(Features.CUSTOMER_ONLINE_PAYMENT)).thenReturn(Observable.just(false))

        val result = isCustomerCashbackFeatureEnabled.execute().subscribeOn(Schedulers.trampoline()).test()

        result.assertValue(false)
    }

    @Test
    fun `When cashback feature is disabled then return false`() {
        whenever(ab.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(false))
        whenever(ab.isFeatureEnabled(Features.CUSTOMER_ONLINE_PAYMENT)).thenReturn(Observable.just(true))

        val result = isCustomerCashbackFeatureEnabled.execute().subscribeOn(Schedulers.trampoline()).test()

        result.assertValue(false)
    }

    @Test
    fun `When both features are disabled then return false`() {
        whenever(ab.isFeatureEnabled(FEATURE_NAME)).thenReturn(Observable.just(false))
        whenever(ab.isFeatureEnabled(Features.CUSTOMER_ONLINE_PAYMENT)).thenReturn(Observable.just(false))

        val result = isCustomerCashbackFeatureEnabled.execute().subscribeOn(Schedulers.trampoline()).test()

        result.assertValue(false)
    }
}
