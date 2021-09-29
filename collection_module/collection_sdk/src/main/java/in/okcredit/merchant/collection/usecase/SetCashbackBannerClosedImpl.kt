package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.SetCashbackBannerClosed
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SetCashbackBannerClosedImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    SetCashbackBannerClosed {
    override fun execute(customerId: String): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            collectionRepository.get().setCashbackBannerClosed(customerId, businessId)
        }
    }
}
