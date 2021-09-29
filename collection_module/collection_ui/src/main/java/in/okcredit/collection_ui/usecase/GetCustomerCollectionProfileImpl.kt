package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.GetCustomerCollectionProfile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCustomerCollectionProfileImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetCustomerCollectionProfile {

    override fun execute(customerId: String): Observable<CollectionCustomerProfile> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionSyncer.get().scheduleCollectionProfileForCustomer(customerId, businessId)
            collectionRepository.get().getCollectionCustomerProfile(customerId, businessId)
        }
    }
}
