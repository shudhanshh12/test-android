package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetLastOnlinePayment @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Observable<CollectionOnlinePayment> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionRepository.get().lastOnlinePayment(businessId)
        }
    }
}
