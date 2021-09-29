package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetTransactionIdFromCollection @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(collectionId: String): Single<Pair<String, String>> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            transactionRepo.get().getTransactionIdFromCollection(collectionId, businessId)
                .onErrorReturn { "" }
                .map { collectionId to it }
        }
    }
}
