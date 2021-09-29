package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import dagger.Lazy
import io.reactivex.Completable
import javax.inject.Inject

class SetOnlinePaymentStatusForRefundTxn @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
) {
    fun execute(
        txnId: String,
        newStatus: Int,
    ): Completable {
        return collectionRepository.get().setOnlinePaymentStatusLocallyForRefundTxn(txnId, newStatus)
    }
}
