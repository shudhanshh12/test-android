package `in`.okcredit.merchant.customer_ui.ui.subscription.usecase

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository

class SubscriptionFeatureEnabledTest {

    private lateinit var subscriptionFeatureEnabled: SubscriptionFeatureEnabled

    private val ab: AbRepository = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        subscriptionFeatureEnabled = SubscriptionFeatureEnabled { ab }
    }

    @Test
    fun `feature enabled returns true`() {
        assert(SubscriptionFeatureEnabled.SUBSCRIPTION_FEATURE == "subscription")

        whenever(ab.isFeatureEnabled("subscription")).thenReturn(Observable.just(true))

        val testObserver = subscriptionFeatureEnabled.execute()

        assert(testObserver.blockingLast() == true)
    }

    @Test
    fun `feature enabled returns false`() {
        assert(SubscriptionFeatureEnabled.SUBSCRIPTION_FEATURE == "subscription")

        whenever(ab.isFeatureEnabled("subscription")).thenReturn(Observable.just(false))

        val testObserver = subscriptionFeatureEnabled.execute()

        assert(testObserver.blockingLast() == false)
    }
}
