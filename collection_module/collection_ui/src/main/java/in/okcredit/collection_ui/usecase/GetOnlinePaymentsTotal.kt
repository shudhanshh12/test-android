package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetOnlinePaymentsTotal @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Double> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            (collectionRepository.getOnlinePaymentsTotalAmount(businessId))
        }
    }
}
