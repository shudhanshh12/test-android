package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CustomerAdditionalInfo
import `in`.okcredit.collection.contract.GetCustomerAdditionalInfoList
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetCustomerAdditionalInfoListImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    GetCustomerAdditionalInfoList {
    override fun execute(): Observable<List<CustomerAdditionalInfo>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            collectionRepository.get().getTargetedReferral(businessId)
        }
    }
}
