package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCollectionsOfCustomerOrSupplier @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(customerId: String): Observable<List<Collection>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionRepository.get().getCollectionsOfCustomerOrSupplier(customerId, businessId)
        }
    }
}
