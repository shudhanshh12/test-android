package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import javax.inject.Inject

class GetLatestOnlinePaymentDate @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<DateTime> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionRepository.get().getLatestOnlinePaymentDate(businessId)
        }
    }
}
