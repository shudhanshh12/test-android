package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncTransactionsImpl
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class GetTxnDetailsUsingCollectionId @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val getCustomer: Lazy<GetCustomer>,
    private val syncTransactionsImpl: Lazy<SyncTransactionsImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(collectionId: String?): Observable<Response> {
        Timber.e("<<<InAppNav GetTxnDetailsUsingCollectionId")

        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            syncTransactionsImpl.get().execute("online payment", businessId = businessId)
                .andThen(
                    transactionRepo.get().isTransactionForCollectionPresent(collectionId ?: "", businessId)
                        .flatMapObservable { isTxnPresent ->
                            if (isTxnPresent) {
                                Timber.e("<<<InAppNav TxnPresent")

                                return@flatMapObservable transactionRepo.get()
                                    .getTransactionUsingCollectionId(collectionId ?: "", businessId)
                                    .flatMap { txn ->
                                        Timber.e("<<<InAppNav amount ${txn.amountV2}, collectionId ${txn.collectionId}")
                                        getCustomer.get().execute(txn.customerId)
                                            .map { Response(txn, it) }
                                    }.doOnError {
                                        Timber.e("<<<InAppNav txn not found")
                                    }
                            } else {
                                Timber.e("<<<InAppNav TransactionNotFountException")
                                throw GetCustomerStatement.TransactionNotFoundException()
                            }
                        }
                )
        }
    }

    data class Response(
        val transaction: merchant.okcredit.accounting.model.Transaction,
        val customer: Customer,
    )
}
