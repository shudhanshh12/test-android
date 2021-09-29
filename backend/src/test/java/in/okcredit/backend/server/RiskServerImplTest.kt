package `in`.okcredit.backend.server

import `in`.okcredit.backend.server.riskInternal.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import tech.okcredit.android.base.error.Error
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.ApiError

class RiskServerImplTest {
    private val apiClient: RiskApiClient = mock()
    private val serverImpl = RiskServerImpl { apiClient }

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    companion object {
        val riskDetailsRequest = RiskDetailsRequest(serviceName = "serviceName", client = "client")
        val kycInfo = KycInfo(kycStatus = "kycStatus")
        val limitInfoDetails = LimitInfoDetails(
            dailyLimitReached = false,
            totalDailyAmountLimit = 0,
            totalDailyLimitUsed = 0,
            remainingDailyAmountLimit = 0,
            totalDailyTransactionLimit = 0,
            remainingDailyTransactionLimit = 0
        )
        val limitInfo = LimitInfo(
            upiLimitInfo = limitInfoDetails,
            nonUpiLimitInfo = limitInfoDetails
        )
        val metaInfo = MetaInfo()
        val paymentInstruments =
            PaymentInstruments(instrumentName = "", enabled = false, metaInfo = metaInfo, limitInfo = limitInfoDetails)
        val futureLimit = FutureLimit(totalAmountLimit = 100, totalTransactionLimit = 10)
        val riskDetailsResponse = RiskDetailsResponse(
            kycInfo = kycInfo,
            riskCategory = "riskCategory",
            limitInfo = limitInfo,
            paymentInstruments = listOf(paymentInstruments),
            futureLimit = futureLimit
        )
    }

    @Test
    fun `when getRiskDetails called return RiskDetails`() {
        val businessId = "business-id"
        // given
        whenever(apiClient.getRiskDetails(riskDetailsRequest, businessId)).thenReturn(
            Single.just(
                Response.success(
                    riskDetailsResponse
                )
            )
        )

        // when
        val result = serverImpl.getRiskDetails(riskDetailsRequest, businessId).test()

        // then
        result.assertValue {
            it.equals(riskDetailsResponse)
        }
    }

    @Test
    fun `when getRiskDetails Called Retrofit retrun ErrorResponse`() {
        val businessId = "business-id"
        // given
        val request: RiskDetailsRequest = mock()
        val mockError: ApiError = mock()
        whenever(apiClient.getRiskDetails(request, businessId)).thenReturn(Single.error(mockError))

        // when
        val testObserver = serverImpl.getRiskDetails(request, businessId).test()

        // then
        testObserver.assertError(mockError)
        verify(apiClient).getRiskDetails(request, businessId)
    }
}
