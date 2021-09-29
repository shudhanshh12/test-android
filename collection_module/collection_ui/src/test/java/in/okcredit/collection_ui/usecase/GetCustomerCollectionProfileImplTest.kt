package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetCustomerCollectionProfileImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val collectionSyncer: CollectionSyncer = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getCustomerCollectionProfile =
        GetCustomerCollectionProfileImpl(
            { collectionRepository },
            { collectionSyncer },
            { getActiveBusinessId }
        )

    @Test
    fun `get customer collection profile`() {
        // Given
        val businessId = "business-id"
        val customerId = "customer-id"
        val collectionCustomerProfile: CollectionCustomerProfile = mock()

        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(collectionRepository.getCollectionCustomerProfile(customerId, businessId))
            .thenReturn(Observable.just(collectionCustomerProfile))
        // When
        val testObserver = getCustomerCollectionProfile.execute(customerId).test()

        // then
        testObserver.assertValue(collectionCustomerProfile)
        verify(getActiveBusinessId).execute()
        verify(collectionSyncer).scheduleCollectionProfileForCustomer(customerId, businessId)
        verify(collectionRepository).getCollectionCustomerProfile(customerId, businessId)
    }
}
