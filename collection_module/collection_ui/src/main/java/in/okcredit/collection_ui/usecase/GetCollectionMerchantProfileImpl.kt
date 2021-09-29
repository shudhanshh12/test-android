package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetCollectionMerchantProfile
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCollectionMerchantProfileImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetCollectionMerchantProfile {

    override fun execute(): Observable<CollectionMerchantProfile> {
        return getActiveBusinessId.get().execute()
            .flatMapObservable { businessId ->
                collectionRepository.get().getCollectionMerchantProfile(businessId)
            }
    }
}
