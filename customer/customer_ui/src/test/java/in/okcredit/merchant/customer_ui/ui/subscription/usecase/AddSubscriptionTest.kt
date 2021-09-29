package `in`.okcredit.merchant.customer_ui.ui.subscription.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AddSubscriptionTest {

    private lateinit var addSubscription: AddSubscription

    private val customerRepositoryImpl: CustomerRepositoryImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        addSubscription = AddSubscription({ customerRepositoryImpl }, { getActiveBusinessId })
    }

    @Test
    fun `customer repository success return subscription`() {
        runBlocking {
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(
                customerRepositoryImpl.addSubscription(
                    customerId = TestData.CUSTOMER.id,
                    amount = TestData.DAILY_SUBSCRIPTION.amount,
                    name = TestData.DAILY_SUBSCRIPTION.name,
                    frequency = SubscriptionFrequency.DAILY,
                    startDate = null,
                    days = null,
                    businessId = businessId
                )
            ).thenReturn(TestData.DAILY_SUBSCRIPTION)

            val testObserver = addSubscription.execute(
                customerId = TestData.CUSTOMER.id,
                amount = TestData.DAILY_SUBSCRIPTION.amount,
                name = TestData.DAILY_SUBSCRIPTION.name,
                frequency = SubscriptionFrequency.DAILY,
                startDate = null,
                days = null
            ).blockingLast()

            assert(testObserver == Result.Success(TestData.DAILY_SUBSCRIPTION))
        }
    }
}
