package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.IsCollectionActivatedOrOnlinePaymentExist
import `in`.okcredit.merchant.collection.CollectionLocalSource
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class IsCollectionActivatedOrOnlinePaymentExistImpl @Inject constructor(
    private val localSource: Lazy<CollectionLocalSource>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : IsCollectionActivatedOrOnlinePaymentExist {
    override fun execute(): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            localSource.get().getCollectionMerchantProfile(businessId)
                .flatMap { profile ->
                    localSource.get().getOnlinePaymentsCount(businessId).map {
                        if (profile.payment_address.isNotBlank()) true
                        else it > 0
                    }
                }
        }
    }
}
