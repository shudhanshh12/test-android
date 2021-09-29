package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import javax.inject.Inject

class GetLatestPaymentAddedByCustomer @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(customerId: String): Single<Transaction> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            transactionRepo.get().getLatestPaymentAddedByCustomer(customerId, businessId)
        }
    }
}
