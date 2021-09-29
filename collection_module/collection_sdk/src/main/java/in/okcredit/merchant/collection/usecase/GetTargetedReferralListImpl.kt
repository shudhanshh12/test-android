package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.COLLECTION_TARGET_REFERRAL
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CustomerAdditionalInfo
import `in`.okcredit.collection.contract.GetTargetedReferralList
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetTargetedReferralListImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val abRepository: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetTargetedReferralList {
    override fun execute(): Observable<List<CustomerAdditionalInfo>> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            abRepository.get().isFeatureEnabled(COLLECTION_TARGET_REFERRAL, businessId = businessId)
                .firstOrError()
                .flatMapObservable {
                    if (it) {
                        collectionRepository.get().getTargetedReferral(businessId)
                    } else Observable.empty()
                }
        }
    }
}
