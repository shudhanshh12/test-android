package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.GetSupplierCollectionProfileWithSync
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class GetSupplierCollectionProfileWithSyncImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetSupplierCollectionProfileWithSync {

    override fun execute(supplierId: String, async: Boolean): Observable<CollectionCustomerProfile> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            if (async) {
                Completable.fromAction {
                    collectionSyncer.get().scheduleCollectionProfileForSupplier(supplierId, businessId)
                }.andThen(collectionRepository.get().getSupplierCollectionProfile(supplierId, businessId))
            } else {
                rxCompletable { collectionSyncer.get().executeSyncCollectionProfileForSupplier(supplierId, businessId) }
                    .andThen(collectionRepository.get().getSupplierCollectionProfile(supplierId, businessId))
            }
        }
    }
}
