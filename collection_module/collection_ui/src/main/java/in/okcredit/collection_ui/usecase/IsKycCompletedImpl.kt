package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.IsKycCompleted
import `in`.okcredit.collection.contract.KycStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class IsKycCompletedImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : IsKycCompleted {
    override fun execute(): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionRepository.get().getKycStatus(businessId).map {
                it == KycStatus.COMPLETE.value
            }
        }
    }
}
