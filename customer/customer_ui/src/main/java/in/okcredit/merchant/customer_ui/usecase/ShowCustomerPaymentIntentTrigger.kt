package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.await
import merchant.okcredit.accounting.model.Transaction
import javax.inject.Inject

class ShowCustomerPaymentIntentTrigger @Inject constructor(
    private val collectionRepository: Lazy<CollectionRepository>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(customerId: String): Observable<Boolean> =
        collectionRepository.get().isCollectionActivated().switchMap { activated ->
            if (activated) return@switchMap Observable.just(false)

            return@switchMap getActiveBusinessId.get().execute()
                .flatMapObservable { businessId ->
                    checkForPaymentIntent(customerId, businessId)
                }
        }

    private fun checkForPaymentIntent(customerId: String, businessId: String) =
        collectionRepository.get().getCollectionCustomerProfile(customerId, businessId).asFlow().combine(
            transactionRepo.get().listTransactions(customerId, businessId).asFlow()
        ) { customerProfile: CollectionCustomerProfile, transactions: List<Transaction> ->
            transformData(
                customerId = customerId,
                customerProfile = customerProfile,
                transactions = transactions,
                businessId = businessId
            )
        }.asObservable()

    private suspend fun transformData(
        customerId: String,
        customerProfile: CollectionCustomerProfile,
        transactions: List<Transaction>,
        businessId: String
    ): Boolean {
        val txnCountForCustomer = customerRepositoryImpl.get().getTxnCountForPaymentIntentEnabled(customerId)
        val currentTxnCount = transactions.size
        // if not triggered via customer then return false
        if (!customerProfile.paymentIntent) {
            return false
        }

        // if current txn count for which payment intent triggered is 0 then set txn count and return true
        if (txnCountForCustomer == 0 || txnCountForCustomer == null) {
            customerRepositoryImpl.get().setTxnCountForPaymentIntent(customerId, currentTxnCount, businessId)
            return true
        }

        // if diff current txn count and count when payment intent was enabled is more than 3 then disable the flag and return false
        if (currentTxnCount - txnCountForCustomer >= MAX_PAYMENT_INTENT_TXN_COUNT_DIFF) {
            collectionRepository.get().updateCustomerPaymentIntent(customerId, false, businessId).await()
            return false
        }

        // if not above conditions are met then return true
        return true
    }

    companion object {
        const val MAX_PAYMENT_INTENT_TXN_COUNT_DIFF = 3
    }
}
