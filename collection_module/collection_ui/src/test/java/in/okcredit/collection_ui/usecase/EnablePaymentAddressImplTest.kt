package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class EnablePaymentAddressImplTest {
    private val repository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val enablePaymentAddressTest = EnablePaymentAddressImpl(repository, { getActiveBusinessId })

    @Test
    fun `enable payment address successfully`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(repository.enableCustomerPayment(businessId))
            .thenReturn(Completable.complete())

        val testObserver = enablePaymentAddressTest.execute().test()

        testObserver.assertComplete()

        verify(repository).enableCustomerPayment(businessId)
        testObserver.dispose()
    }

    @Test(expected = Exception::class)
    fun `enable payment address Error`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        whenever(repository.enableCustomerPayment(businessId))
            .thenThrow(Exception())

        val testObserver = enablePaymentAddressTest.execute().test()

        testObserver.assertError(java.lang.Exception())

        verify(repository).enableCustomerPayment(businessId)
        testObserver.dispose()
    }
}
