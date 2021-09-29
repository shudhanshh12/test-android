package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SetPaymentTagViewed @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            collectionRepository.get().setOnlinePaymentTag(businessId)
        }
    }
}
