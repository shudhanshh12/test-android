package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.SetOnlinePaymentStatusLocally
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SetOnlinePaymentStatusLocallyImpl @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : SetOnlinePaymentStatusLocally {
    override fun execute(
        oldStatus: Int,
        newStatus: Int,
    ): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            collectionRepository.get().setOnlinePaymentStatusLocallyForAllOlderTxn(oldStatus, newStatus, businessId)
        }
    }
}
