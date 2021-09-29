package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetBlindPayShareLink
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetBlindPayShareLinkImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetBlindPayShareLink {
    override fun execute(paymentId: String): Single<String> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            collectionRepository.get().getBlindPayShareLink(paymentId, businessId).map {
                it.shareLink
            }
        }
    }
}
