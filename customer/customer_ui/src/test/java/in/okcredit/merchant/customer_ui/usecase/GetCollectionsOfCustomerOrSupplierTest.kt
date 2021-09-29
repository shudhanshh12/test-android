package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetCollectionsOfCustomerOrSupplierTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getCollectionsOfCustomerOrSupplier = GetCollectionsOfCustomerOrSupplier(
        { collectionRepository },
        { getActiveBusinessId }
    )

    @Test
    fun `execute should return list of customers or suppliers`() {
        val customerId = "customer-id"
        val businessId = "business-id"
        val collectionList: List<Collection> = mock()
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getCollectionsOfCustomerOrSupplier(customerId, businessId))
            .thenReturn(Observable.just(collectionList))

        val testObserver = getCollectionsOfCustomerOrSupplier.execute(customerId).test()

        verify(getActiveBusinessId).execute()
        verify(collectionRepository).getCollectionsOfCustomerOrSupplier(customerId, businessId)
        testObserver.assertValue(collectionList)
    }
}
