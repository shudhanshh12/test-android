package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetBlindPayLinkId
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetBlindPayLinkIdImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetBlindPayLinkId {
    override fun execute(accountId: String): Single<String> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            collectionRepository.get().getBlindPayLinkId(accountId, businessId).map {
                it.linkId
            }
        }
    }
}
