package `in`.okcredit.cashback

import `in`.okcredit.cashback.datasource.remote.CashbackRemoteSourceImpl
import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackApiClient
import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackMessageDetailsDto
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import retrofit2.Response
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError

class CashbackRemoteSourceTest {

    private val cashbackApiClient: CashbackApiClient = mock()
    private val schedulerProvider: SchedulerProvider = mock()
    private val cashbackRemoteSource = CashbackRemoteSourceImpl { cashbackApiClient }
    private val businessId = "businessId"

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `test 2xx success response`() {
        runBlocking {
            val response = TestData.cashbackMessageDetailsDto
            whenever(cashbackApiClient.getCashbackMessageDetails(businessId)).thenReturn(Single.just(Response.success(response)))

            cashbackRemoteSource.getCashbackMessageDetails(businessId).test().apply {
                assertValue(response)
                dispose()
            }
        }
    }

    @Test
    fun `test non-2xx error response`() {
        runBlocking {
            val response = Response.error<CashbackMessageDetailsDto>(
                500,
                "Internal Server Error".toResponseBody("text/plain".toMediaType())
            )
            whenever(cashbackApiClient.getCashbackMessageDetails(businessId)).thenReturn(
                Single.just(response)
            )

            cashbackRemoteSource.getCashbackMessageDetails(businessId).test().apply {
                assertError(response.asError())
                dispose()
            }
        }
    }

    @Test
    fun `test incompatible response payload`() {
        runBlocking {
            val error = RuntimeException("Incompatible response received")
            whenever(cashbackApiClient.getCashbackMessageDetails(businessId)).thenReturn(
                Single.error(error)
            )

            cashbackRemoteSource.getCashbackMessageDetails(businessId).test().apply {
                assertError(error)
                dispose()
            }
        }
    }

    // TODO: incomplete test
    @Test
    fun `test - fetch cashback reward`() {
        val dummyPaymentId = "DUMMY_PAYMENT_ID"
        val response = TestData.rewardFromApi

        `when`(cashbackApiClient.getCashbackRewardForPaymentId(dummyPaymentId, businessId))
            .thenReturn(
                Single.just(
                    Response.error(
                        500,
                        "1 Internal Server Error".toResponseBody("text/plain".toMediaType())
                    )
                )
            )
            .thenReturn(
                Single.just(
                    Response.error(
                        500,
                        "2 Internal Server Error".toResponseBody("text/plain".toMediaType())
                    )
                )
            )
            .thenReturn(Single.just(Response.success(response)))

        cashbackRemoteSource.getCashbackRewardForPaymentId(dummyPaymentId, 500, 5000, businessId).test().apply {
        }
    }
}
