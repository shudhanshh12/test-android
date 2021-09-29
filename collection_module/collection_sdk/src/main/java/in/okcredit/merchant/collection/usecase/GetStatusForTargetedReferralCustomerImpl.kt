package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.COLLECTION_TARGET_REFERRAL
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetStatusForTargetedReferralCustomer
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetStatusForTargetedReferralCustomerImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val abRepository: Lazy<AbRepository>,
) : GetStatusForTargetedReferralCustomer {
    override fun execute(customerId: String): Observable<Int> {
        return abRepository.get().isFeatureEnabled(COLLECTION_TARGET_REFERRAL)
            .flatMapSingle { isEnabled ->
                if (isEnabled) {
                    collectionRepository.get().getStatusForTargetedReferralCustomer(customerId)
                } else Single.just(0)
            }
    }
}
