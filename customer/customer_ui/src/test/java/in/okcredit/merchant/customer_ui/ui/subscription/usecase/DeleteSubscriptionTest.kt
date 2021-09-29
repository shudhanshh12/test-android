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
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test

class DeleteSubscriptionTest {

    private lateinit var deleteSubscription: DeleteSubscription

    private val customerRepositoryImpl: CustomerRepositoryImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessId = "businessId"

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        deleteSubscription = DeleteSubscription({ customerRepositoryImpl }, { getActiveBusinessId })
    }

    @Test
    fun `customer repository success return subscription`() {
        runBlocking {
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(
                customerRepositoryImpl.deleteSubscription(
                    TestData.DAILY_SUBSCRIPTION.copy(status = 2),
                    businessId = businessId
                )
            ).thenReturn("Success".toResponseBody())

            val testObserver = deleteSubscription.execute(
                TestData.DAILY_SUBSCRIPTION.copy(status = 2)
            ).blockingLast()

            assert(testObserver == Result.Success(Unit))
        }
    }
}
