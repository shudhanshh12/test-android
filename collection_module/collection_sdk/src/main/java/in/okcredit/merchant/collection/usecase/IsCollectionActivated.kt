package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.merchant.collection.CollectionLocalSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class IsCollectionActivated @Inject constructor(
    private val localSource: Lazy<CollectionLocalSource>, // TODO refactor this
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(): Observable<Boolean> = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        localSource.get().getCollectionMerchantProfile(businessId)
            .map { it.payment_address.isNotBlank() }
    }
}
