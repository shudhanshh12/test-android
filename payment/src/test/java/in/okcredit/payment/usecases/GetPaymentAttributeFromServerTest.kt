package `in`.okcredit.payment.usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.PaymentTestData.PaymentAttributesResponse
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.base.network.NetworkError

class GetPaymentAttributeFromServerTest {

    private val repository: PaymentRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getJuspayAttributeFromServer =
        GetPaymentAttributeFromServerImpl(Lazy { repository }, { getActiveBusinessId })

    @Test
    fun `getPaymentAttributes() returns data successfully`() {
        val response: PaymentApiMessages.GetPaymentAttributesResponse = PaymentAttributesResponse
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(repository.getPaymentAttributes("client_id", "link_id", businessId))
            .thenReturn(Single.just(response))

        val testObserver = getJuspayAttributeFromServer.execute("client_id", "link_id").test()

        testObserver.assertValue { it.paymentId == "payment_id" && it.pollingType == "polling" }

        verify(repository).getPaymentAttributes("client_id", "link_id", businessId)
    }

//    @Test
//    fun `getPaymentAttributes returns auth error`() {
//
//        val mockError = Unauthorized()
//
//        whenever(api.getPaymentAttributes("client_id", "link_id"))
//            .thenReturn(Single.error(mockError))
//
//        val testObserver = getJuspayAttributeFromServer.execute("client_id", "link_id").test()
//
//        testObserver.assertValues(
//            `in`.okcredit.shared.usecase.Result.Progress(),
//            `in`.okcredit.shared.usecase.Result.Failure(mockError)
//        )
//
//        verify(api).getPaymentAttributes("client_id", "link_id")
//    }

    @Test
    fun `getPaymentAttributes returns network error`() {

        val mockError = NetworkError("network error", cause = Throwable("network error"))
        val businessId = "business-id"

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(repository.getPaymentAttributes("client_id", "link_id", businessId))
            .thenReturn(Single.error(mockError))

        val testObserver = getJuspayAttributeFromServer.execute("client_id", "link_id").test()

        testObserver.assertError { it is NetworkError }

        verify(repository).getPaymentAttributes("client_id", "link_id", businessId)
    }
}
