package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

class TagOnlinePaymentWithCustomerTest {

    private val collectionRepositoryLazy: Lazy<CollectionRepository> = mock()
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val tagOnlinePaymentWithCustomer = TagOnlinePaymentWithCustomer(collectionRepositoryLazy, { getActiveBusinessId })

    @Before
    fun setup() {
        whenever(collectionRepositoryLazy.get())
            .thenReturn(collectionRepository)
    }

    @Test
    fun `execute should complete`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepositoryLazy.get().tagMerchantPaymentWithCustomer("customerId", "paymentId", businessId))
            .thenReturn(Completable.complete())

        val testObserver =
            tagOnlinePaymentWithCustomer.execute(TagOnlinePaymentWithCustomer.Request("customerId", "paymentId")).test()

        verify(collectionRepositoryLazy.get()).tagMerchantPaymentWithCustomer("customerId", "paymentId", businessId)
        testObserver.assertComplete()
        testObserver.dispose()
    }
}
