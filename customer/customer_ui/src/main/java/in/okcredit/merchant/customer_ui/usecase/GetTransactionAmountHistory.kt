package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class GetTransactionAmountHistory @Inject constructor(
    private val coreSdk: CoreSdk,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(transactionId: String): Single<TransactionAmountHistory> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            coreSdk.isCoreSdkFeatureEnabled(businessId)
                .flatMap { isCoreSdkFeatureEnabled ->
                    if (isCoreSdkFeatureEnabled) {
                        coreSdk.getTransaction(transactionId, businessId)
                            .firstOrError()
                            .flatMap {
                                if (it.amountUpdated) {
                                    coreSdk.getTxnAmountHistory(transactionId, businessId)
                                } else {
                                    throw TransactionHistoryNotFountException()
                                }
                            }
                    } else {
                        throw TransactionHistoryNotFountException()
                    }
                }
        }
    }

    class TransactionHistoryNotFountException : Exception()
}
