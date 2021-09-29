package `in`.okcredit.frontend.usecase

import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetSupplierCollection @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val collectionSyncer: CollectionSyncer,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(collectionId: String): Observable<Result<Collection>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                collectionSyncer.scheduleSyncCollections(
                    CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS,
                    CollectionSyncer.Source.SUPPLIER_SCREEN,
                    businessId
                )
                collectionRepository.getCollection(collectionId, businessId)
            }
        )
    }
}
