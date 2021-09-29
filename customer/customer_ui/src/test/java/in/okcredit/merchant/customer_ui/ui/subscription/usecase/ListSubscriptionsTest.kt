package `in`.okcredit.merchant.customer_ui.ui.subscription.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.TestData
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
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

class ListSubscriptionsTest {
    private lateinit var listSubscriptions: ListSubscriptions

    private val customerRepositoryImpl: CustomerRepositoryImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        listSubscriptions = ListSubscriptions({ customerRepositoryImpl }, { getActiveBusinessId })
    }

    @Test
    fun `customer repository success return subscription list`() {
        runBlocking {
            val businessId = "businessId"
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(
                customerRepositoryImpl.listSubscriptions(
                    customerId = TestData.CUSTOMER.id,
                    businessId = TestData.BUSINESS_ID
                )
            ).thenReturn(
                listOf(
                    TestData.DAILY_SUBSCRIPTION,
                    TestData.MONTHLY_SUBSCRIPTION,
                    TestData.WEEKLY_SUBSCRIPTION
                )
            )

            val testObserver = listSubscriptions.execute(
                customerId = TestData.CUSTOMER.id
            ).blockingLast()

            assert(
                testObserver == Result.Success(
                    listOf(
                        TestData.DAILY_SUBSCRIPTION,
                        TestData.MONTHLY_SUBSCRIPTION,
                        TestData.WEEKLY_SUBSCRIPTION
                    )
                )
            )
        }
    }
}
