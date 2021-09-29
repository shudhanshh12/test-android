package `in`.okcredit.payment.usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.contract.model.PaymentModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class JuspayPaymentPollingFromServerTest {

    private val repository: PaymentRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val juspayPaymentPollingFromServer = GetPaymentResultImpl({ repository }, { getActiveBusinessId })

    @Test
    fun `JuspayPaymentPollingFromServer() returns data successfully`() {
        val response: PaymentModel.JuspayPaymentPollingModel = mock()
        val businessId = "business-id"

        whenever(repository.getJuspayPaymentPolling("p_id", true, "type", businessId))
            .thenReturn(Observable.just(response))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val testObserver = juspayPaymentPollingFromServer.execute("p_id", true, "type").test()

        testObserver.assertValues(
            response
        )

        verify(repository).getJuspayPaymentPolling("p_id", true, "type", businessId)
    }

    @Test(expected = Exception::class)
    fun `JuspayPaymentPollingFromServer() returns error`() {

        val businessId = "business-id"
        whenever(repository.getJuspayPaymentPolling("p_id", true, "type", businessId))
            .thenThrow(java.lang.Exception())

        val testObserver = juspayPaymentPollingFromServer.execute("p_id", true, "type").test()

        testObserver.assertError(java.lang.Exception())

        verify(repository).getJuspayPaymentPolling("p_id", true, "type", businessId)
    }
}
