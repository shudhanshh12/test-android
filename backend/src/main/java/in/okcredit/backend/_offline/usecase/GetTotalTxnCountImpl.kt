package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Single
import javax.inject.Inject

@Reusable
class GetTotalTxnCountImpl @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetTotalTxnCount {
    override fun execute(): Single<Int> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            transactionRepo.allTransactionsCount(businessId)
        }
    }
}
