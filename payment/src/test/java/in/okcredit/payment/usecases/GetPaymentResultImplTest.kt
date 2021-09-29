package `in`.okcredit.payment.usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.payment.PaymentRepository
import `in`.okcredit.payment.contract.model.PaymentModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class GetPaymentResultImplTest {
    private val repository: PaymentRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    lateinit var getPaymentResultImpl: GetPaymentResultImpl

    @Before
    fun setUp() {
        getPaymentResultImpl = GetPaymentResultImpl({ repository }, { getActiveBusinessId })
    }

    companion object {
        val pid = "pid"
        val juspayPaymentPollingModel: PaymentModel.JuspayPaymentPollingModel = mock()
    }

    @Test
    fun `test Execute`() {
        // given
        whenever(repository.getJuspayPaymentPolling(pid, true, "type", "business-id"))
            .thenReturn(Observable.just(juspayPaymentPollingModel))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just("business-id"))
        // when
        val result = getPaymentResultImpl.execute(pid, true, "type").test()

        // then
        result.assertValues(
            juspayPaymentPollingModel
        )
    }
}
