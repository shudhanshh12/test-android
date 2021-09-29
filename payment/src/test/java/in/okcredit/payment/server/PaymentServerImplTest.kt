package `in`.okcredit.payment.server

import `in`.okcredit.payment.server.internal.PaymentApiClient
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
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

class PaymentServerImplTest {
    private val apiClient: PaymentApiClient = mock()
    private val paymentServerImpl: PaymentServerImpl = PaymentServerImpl(Lazy { apiClient })

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `getJuspayAttributes() when api call successful then return response body`() {
        val request: PaymentApiMessages.JuspayAttributeRequestBody = mock()
        val response: PaymentApiMessages.GetJuspayAttributesResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.getJuspayAttributes(request, businessId)).thenReturn(Single.just(Response.success(response)))

        val testObserver = paymentServerImpl.getJuspayAttributes(request, businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).getJuspayAttributes(request, businessId)
    }

    @Test
    fun `getJuspayAttributes() when api call unsuccessful then return error`() {
        val request: PaymentApiMessages.JuspayAttributeRequestBody = mock()
        val response: Response<PaymentApiMessages.GetJuspayAttributesResponse> = mock()
        val businessId = "business-id"
        whenever(apiClient.getJuspayAttributes(any(), eq(businessId))).thenReturn(Single.just(response))
        whenever(response.isSuccessful).thenReturn(false)
        val mockError: Error = mock()
        every { Error.parse(response) } returns mockError

        val testObserver = paymentServerImpl.getJuspayAttributes(request, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getJuspayAttributes(request, businessId)
    }

    @Test
    fun `getJuspayAttributes() when response body is null then return error`() {

        val request: PaymentApiMessages.JuspayAttributeRequestBody = mock()
        val response: Response<PaymentApiMessages.GetJuspayAttributesResponse> = mock()
        val businessId = "business-id"
        whenever(apiClient.getJuspayAttributes(any(), eq(businessId))).thenReturn(Single.just(response))
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(null)
        val mockError: Error = mock()
        every { Error.parse(response) } returns mockError

        val testObserver = paymentServerImpl.getJuspayAttributes(request, businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getJuspayAttributes(request, businessId)
    }

    @Test
    fun `getPaymentAttributes() when api call successful then return response body`() {
        val request: PaymentApiMessages.PaymentAttributeRequestBody = mock()
        val response: PaymentApiMessages.GetPaymentAttributesResponse = mock()
        val businessId = "business-id"
        whenever(apiClient.getPaymentAttributes(any(), any(), eq(businessId))).thenReturn(
            Single.just(
                Response.success(
                    response
                )
            )
        )

        val testObserver = paymentServerImpl.getPaymentAttributes(request, "link_id", businessId).test()

        testObserver.assertValue(response)
        verify(apiClient, times(1)).getPaymentAttributes(request, "link_id", businessId)
    }

    @Test
    fun `getPaymentAttributes() when api call unsuccessful then return error`() {
        val request: PaymentApiMessages.PaymentAttributeRequestBody = mock()
        val response: Response<PaymentApiMessages.GetPaymentAttributesResponse> = mock()
        val businessId = "business-id"
        whenever(apiClient.getPaymentAttributes(any(), any(), eq(businessId))).thenReturn(Single.just(response))
        whenever(response.isSuccessful).thenReturn(false)
        val mockError: Error = mock()
        every { Error.parse(response) } returns mockError

        val testObserver = paymentServerImpl.getPaymentAttributes(request, "link_id", businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getPaymentAttributes(request, "link_id", businessId)
    }

    @Test
    fun `getPaymentAttributes() when response body is null then return error`() {
        val request: PaymentApiMessages.PaymentAttributeRequestBody = mock()
        val response: Response<PaymentApiMessages.GetPaymentAttributesResponse> = mock()
        val businessId = "business-id"
        whenever(apiClient.getPaymentAttributes(any(), any(), eq(businessId))).thenReturn(Single.just(response))
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(null)
        val mockError: Error = mock()
        every { Error.parse(response) } returns mockError

        val testObserver = paymentServerImpl.getPaymentAttributes(request, "link_id", businessId).test()

        testObserver.assertError(mockError)
        verify(apiClient, times(1)).getPaymentAttributes(request, "link_id", businessId)
    }

    @Test
    fun `createPaymentDestination() when api call successful then return response body`() {
        val request: PaymentApiMessages.PaymentDestinationRequest = mock()
        val response: Response<PaymentApiMessages.PaymentDestinationResponse> = mock()
        val businessId = "business-id"
        whenever(apiClient.createPaymentDestination(request.destinationId, request, businessId)).thenReturn(Single.just(response))

        val testObserver = paymentServerImpl.createPaymentDestination(request, businessId).test()

        testObserver.assertValue(response)

        verify(apiClient, times(1)).createPaymentDestination(request.destinationId, request, businessId)
    }

    @Test(expected = Exception::class)
    fun `createPaymentDestination() when api call unsuccessful then return error`() {

        val request: PaymentApiMessages.PaymentDestinationRequest = mock()
        val businessId = "business-id"

        whenever(apiClient.createPaymentDestination(request.destinationId, request, businessId)).thenThrow(Exception())

        val testObserver = paymentServerImpl.createPaymentDestination(request, businessId).test()

        testObserver.assertError(java.lang.Exception())

        verify(apiClient, times(1)).createPaymentDestination(request.destinationId, request, businessId)
    }
}
