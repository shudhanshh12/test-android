package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class IsNetworkReminderEnabledImplTest {
    private val ab: AbRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val isNetworkReminderEnabledImpl = IsNetworkReminderEnabledImpl(
        { ab },
        { getActiveBusinessId }
    )

    @Test
    fun `test execute when account chat enabled`() {
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(ab.isFeatureEnabled(IsNetworkReminderEnabledImpl.FEATURE_NETWORK_REMINDER, businessId = businessId))
            .thenReturn(Observable.just(true))

        val result = isNetworkReminderEnabledImpl.execute().test()

        result.assertValue(true)

        result.dispose()
    }

    @Test
    fun `test execute when account chat disabled`() {
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(ab.isFeatureEnabled(IsNetworkReminderEnabledImpl.FEATURE_NETWORK_REMINDER, businessId = businessId))
            .thenReturn(Observable.just(false))

        val result = isNetworkReminderEnabledImpl.execute().test()

        result.assertValue(false)

        result.dispose()
    }
}
