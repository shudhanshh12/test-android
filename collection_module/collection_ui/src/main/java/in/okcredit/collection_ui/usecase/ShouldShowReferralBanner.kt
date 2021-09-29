package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.COLLECTION_TARGET_REFERRAL
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class ShouldShowReferralBanner @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val abRepository: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            abRepository.get().isFeatureEnabled(COLLECTION_TARGET_REFERRAL).flatMap { isEnabled ->
                if (isEnabled) {
                    collectionRepository.get().getTargetedReferral(businessId).map {
                        it.isNotEmpty()
                    }
                } else Observable.just(false)
            }
        }
    }
}
