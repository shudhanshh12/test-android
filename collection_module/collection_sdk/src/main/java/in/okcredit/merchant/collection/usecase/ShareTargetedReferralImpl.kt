package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.ShareTargetedReferral
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class ShareTargetedReferralImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : ShareTargetedReferral {
    override fun execute(customerMerchantId: String): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            collectionRepository.get().shareTargetedReferral(customerMerchantId, businessId)
        }
    }
}
