package `in`.okcredit.payment.usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.PaymentTestData
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test

class SetPaymentDestinationToServerTest {
    private val repository: PaymentRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val setPaymentDestinationToServer = SetPaymentDestinationToServer({ repository }, { getActiveBusinessId })

    @Test
    fun `SetPaymentDestinationToServer() returns data successfully`() {
        val response: PaymentApiMessages.PaymentDestinationResponse = mock()
        val request = PaymentApiMessages.PaymentDestinationRequest(
            serviceName = "TEST",
            destination = PaymentApiMessages.DestinationRequest(
                type = "bank",
                paymentAddress = "1234567@HDFC00012"
            ),
            destinationId = PaymentTestData.MERCHANT.id,
            status = "ACTIVE",
            type = "MERCHANT"
        )

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(PaymentTestData.MERCHANT.id))

        whenever(
            repository.createPaymentDestination(
                request,
                PaymentTestData.MERCHANT.id
            )
        )
            .thenReturn(Single.just(response))

        val testObserver = setPaymentDestinationToServer.execute("TEST", "1234567@HDFC00012", "bank").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(response)
        )

        verify(repository).createPaymentDestination(
            request,
            PaymentTestData.MERCHANT.id
        )
    }

    @Test(expected = Exception::class)
    fun `SetPaymentDestinationToServer() returns error`() {
        val request = PaymentApiMessages.PaymentDestinationRequest(
            serviceName = "TEST",
            destination = PaymentApiMessages.DestinationRequest(
                type = "bank",
                paymentAddress = "1234567@HDFC00012"
            ),
            destinationId = PaymentTestData.MERCHANT.id,
            status = "ACTIVE",
            type = "MERCHANT"
        )

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(PaymentTestData.MERCHANT.id))

        whenever(
            repository.createPaymentDestination(
                request,
                PaymentTestData.MERCHANT.id
            )
        )
            .thenThrow(java.lang.Exception())

        val testObserver = setPaymentDestinationToServer.execute("TEST", "1234567@HDFC00012", "bank").test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Failure(java.lang.Exception())
        )

        verify(repository).createPaymentDestination(
            request,
            PaymentTestData.MERCHANT.id
        )
    }
}
