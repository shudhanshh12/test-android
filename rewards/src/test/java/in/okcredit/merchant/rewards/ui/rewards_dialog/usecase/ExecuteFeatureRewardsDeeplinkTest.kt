package `in`.okcredit.merchant.rewards.ui.rewards_dialog.usecase

import `in`.okcredit.shared.deeplink.InternalDeeplinkNavigationDelegator
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import org.junit.Test

class ExecuteFeatureRewardsDeeplinkTest {
    private val internalDeeplinkNavigator: InternalDeeplinkNavigationDelegator = mock()
    private val executeFeatureRewardsDeeplink = ExecuteFeatureRewardsDeeplink(Lazy { internalDeeplinkNavigator })

    @Test
    fun `should be execute deeplink`() {
        val fakeDeeplink =
            "https://staging.okcredit.app/merchant/v1/web/https%3A%2F%2Fpreprod2.kukufm.com%2F%3Futm_source%3Dokcredit"
        val result = executeFeatureRewardsDeeplink.execute(fakeDeeplink).test()

        verify(internalDeeplinkNavigator).executeDeeplink(fakeDeeplink)
        result.assertValues(
            Result.Progress(),
            Result.Success(Unit)
        )
    }

    @Test
    fun `should be execute deeplink even if unknown deeplink comes`() {
        val fakeDeeplink = ""
        whenever(internalDeeplinkNavigator.executeDeeplink(fakeDeeplink)).thenThrow(IllegalArgumentException("Unknown deeplink $fakeDeeplink"))
        val result = executeFeatureRewardsDeeplink.execute(fakeDeeplink).test()

        verify(internalDeeplinkNavigator).executeDeeplink(fakeDeeplink)
        result.assertValues(
            Result.Progress(),
            Result.Success(Unit)
        )
    }
}
