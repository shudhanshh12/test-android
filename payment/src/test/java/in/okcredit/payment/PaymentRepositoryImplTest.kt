package `in`.okcredit.payment

import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.payment.server.PaymentServerImpl
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Response.success
import tech.okcredit.android.base.error.Error
import tech.okcredit.android.base.utils.ThreadUtils

class PaymentRepositoryImplTest {

    private val context: Context = mock()
    private val server: PaymentServerImpl = mock()
    private val syncer: CollectionSyncer = mockk()
    private val paymentApiImpl: PaymentRepositoryImpl =
        PaymentRepositoryImpl(
            context = { context },
            server = { server },
            collectionSyncer = { syncer }
        )

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        whenever(context.getString(R.string.payment_error_not_able_to_Add_details)).thenReturn("Unable to add your details,Please try again!")
    }

    @Test
    fun `getPaymentAttributes() when api call successful then return response body`() {
        val request = PaymentApiMessages.PaymentAttributeRequestBody("client_id")
        val response: PaymentApiMessages.GetPaymentAttributesResponse = mock()
        val businessId = "business-id"
        whenever(server.getPaymentAttributes(request, "link_id", businessId)).thenReturn(Single.just(response))

        val testObserver = paymentApiImpl.getPaymentAttributes("client_id", "link_id", businessId).test()

        testObserver.assertValue(response)
        verify(server, times(1)).getPaymentAttributes(request, "link_id", businessId)
    }

    @Test(expected = Exception::class)
    fun `getPaymentAttributes() when api call unsuccessful then return error`() {
        val request: PaymentApiMessages.PaymentAttributeRequestBody = mock()
        val businessId = "business-id"

        whenever(server.getPaymentAttributes(request, any(), businessId)).thenThrow(java.lang.Exception())

        val testObserver = paymentApiImpl.getPaymentAttributes(any(), any(), businessId).test()

        testObserver.assertError(java.lang.Exception())

        verify(server, times(1)).getPaymentAttributes(request, any(), businessId)
    }

    @Test
    fun `createPaymentDestination() when api call successful then return response body`() {
        val request: PaymentApiMessages.PaymentDestinationRequest = mock()
        val response: PaymentApiMessages.PaymentDestinationResponse = mock()
        val businessId = "business-id"
        whenever(server.createPaymentDestination(request, businessId)).thenReturn(Single.just(success(response)))
        justRun { (syncer).scheduleCollectionProfile("payment_repo", businessId) }
        val testObserver = paymentApiImpl.createPaymentDestination(request, businessId).test()

        testObserver.assertValue(response)
        verify(server, times(1)).createPaymentDestination(request, businessId)
        io.mockk.verify { (syncer).scheduleCollectionProfile("payment_repo", businessId) }
    }

    @Test
    fun `createPaymentDestination() when api call unsuccessful then return 500 error`() {
        val request: PaymentApiMessages.PaymentDestinationRequest = mock()
        val response: Response<PaymentApiMessages.PaymentDestinationResponse> = mock()
        val businessId = "business-id"
        whenever(server.createPaymentDestination(request, businessId)).thenReturn(Single.just(response))

        whenever(response.isSuccessful).thenReturn(false)
        val mockError = Error(500, "Some Error")
        every { Error.parse(response) } returns mockError

        val testObserver = paymentApiImpl.createPaymentDestination(request, businessId).test()

        testObserver.assertErrorMessage("Unable to add your details,Please try again!")
        verify(server, times(1)).createPaymentDestination(request, businessId)
    }

    @Test
    fun `createPaymentDestination() when api call unsuccessful then return other than 500 error`() {
        val request: PaymentApiMessages.PaymentDestinationRequest = mock()
        val response: Response<PaymentApiMessages.PaymentDestinationResponse> = mock()
        val businessId = "business-id"
        whenever(server.createPaymentDestination(request, businessId)).thenReturn(Single.just(response))

        whenever(response.isSuccessful).thenReturn(false)
        val mockError = Error(400, "Some Error")
        every { Error.parse(response) } returns mockError

        val testObserver = paymentApiImpl.createPaymentDestination(request, businessId).test()

        testObserver.assertErrorMessage("Some Error")
        verify(server, times(1)).createPaymentDestination(request, businessId)
    }

    @Test
    fun `createPaymentDestination() when api call return null body`() {
        val request: PaymentApiMessages.PaymentDestinationRequest = mock()
        val response: Response<PaymentApiMessages.PaymentDestinationResponse> = mock()
        val businessId = "business-id"
        whenever(server.createPaymentDestination(request, businessId)).thenReturn(Single.just(response))

        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(null)
        val mockError = Error(0, "")
        every { Error.parse(response) } returns mockError

        val testObserver = paymentApiImpl.createPaymentDestination(request, businessId).test()

        testObserver.assertErrorMessage("")
        verify(server, times(1)).createPaymentDestination(request, businessId)
    }
}
