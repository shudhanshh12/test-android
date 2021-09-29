package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.GetCashbackBannerClosed
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetCashbackBannerClosedImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    GetCashbackBannerClosed {
    override fun execute(customerId: String): Single<Boolean> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            collectionRepository.get().getCashbackBannerClosed(customerId, businessId)
        }
    }
}
