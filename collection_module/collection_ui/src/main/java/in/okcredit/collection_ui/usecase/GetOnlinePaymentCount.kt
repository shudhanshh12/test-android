package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import javax.inject.Inject

class GetOnlinePaymentCount @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val collectionRepository: Lazy<CollectionRepository>,
) {

    fun execute() = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        collectionRepository.get().getOnlinePaymentCount(businessId).distinctUntilChanged { t1, t2 -> t1 == t2 }
    }
}
