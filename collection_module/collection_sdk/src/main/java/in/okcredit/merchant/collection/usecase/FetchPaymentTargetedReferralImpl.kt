package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.COLLECTION_TARGET_REFERRAL
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.FetchPaymentTargetedReferral
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class FetchPaymentTargetedReferralImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val abRepository: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    FetchPaymentTargetedReferral {
    override fun execute(): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            abRepository.get().isFeatureEnabled(COLLECTION_TARGET_REFERRAL, businessId = businessId)
                .flatMapCompletable {
                    if (it) {
                        collectionRepository.get().fetchTargetedReferral(businessId)
                    } else Completable.complete()
                }
        }
    }
}
