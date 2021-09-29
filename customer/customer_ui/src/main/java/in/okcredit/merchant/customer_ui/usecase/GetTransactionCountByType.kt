package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetTransactionCountByType @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(req: Int): Single<Int> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            transactionRepo.get().getTransactionCountByType(req, businessId)
        }
    }
}
