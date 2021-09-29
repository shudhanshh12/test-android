package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetUnSettledAmountDueToInvalidBankDetails @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(errorCode: String, status: Int): Observable<Double> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionRepository.get().getUnsettledOnlinePaymentAmount(errorCode, status, businessId)
        }
    }
}
