package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.model.TransactionWrapper
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomer
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.util.Pair
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class GetAccountStatement @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val syncCustomer: Lazy<SyncCustomer>,
    private val transactionRepo: Lazy<TransactionRepo>,
    private val collectionsAPI: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        const val TAG = "<<<<GetAccountStatement"
        const val INITIAL_LOADING_COUNT = 1500
    }

    fun execute(request: Request): Observable<Response> {
        Timber.d("$TAG started Executing txns between $request.startTime and $request.endTime")
        return getActiveBusinessId.get().execute()
            .flatMapObservable { businessId ->
                Observable.combineLatest(
                    getTransactions(request.startTime, request.endTime, businessId),
                    customerRepo.get().listCustomers(businessId),
                    { transactions, customers -> process(customers, transactions) }
                )
                    .flatMap { res ->
                        var syncMissingCustomers = Completable.complete()
                        val missingCustomers = res.second
                        for (customerId in missingCustomers) {
                            syncMissingCustomers =
                                syncMissingCustomers.andThen(
                                    syncCustomer.get().schedule(customerId, businessId)
                                )
                        }

                        var paymentAmount: Long = 0
                        var paymentCount = 0
                        var creditCount = 0
                        var creditAmount: Long = 0
                        var discountAmount: Long = 0
                        var discountCount = 0

                        for (transaction in res.first) {
                            if (transaction.transaction.type == Transaction.CREDIT) {
                                creditCount++
                                creditAmount += transaction.transaction.amountV2
                            } else if ((transaction.transaction.type == Transaction.PAYMENT || transaction.transaction.type == Transaction.RETURN) && transaction.transaction.transactionCategory != Transaction.DISCOUNT) {
                                paymentCount++
                                paymentAmount += transaction.transaction.amountV2
                            }
                            if (transaction.transaction.transactionCategory == Transaction.DISCOUNT) {
                                discountCount++
                                discountAmount += transaction.transaction.amountV2
                            }
                        }
                        if (request.isShowAll) {
                            syncMissingCustomers.andThen(
                                Observable.just(
                                    Response(
                                        res.first,
                                        paymentAmount,
                                        paymentCount,
                                        creditCount,
                                        creditAmount,
                                        false,
                                        discountAmount,
                                        discountCount
                                    )
                                )
                            )
                        } else {
                            if (res.first.size > INITIAL_LOADING_COUNT) {
                                syncMissingCustomers.andThen(
                                    Observable.just(
                                        Response(
                                            res.first.subList(0, INITIAL_LOADING_COUNT - 1),
                                            paymentAmount,
                                            paymentCount,
                                            creditCount,
                                            creditAmount,
                                            true,
                                            discountAmount,
                                            discountCount
                                        )
                                    )
                                )
                            } else {
                                syncMissingCustomers.andThen(
                                    Observable.just(
                                        Response(
                                            res.first,
                                            paymentAmount,
                                            paymentCount,
                                            creditCount,
                                            creditAmount,
                                            false,
                                            discountAmount,
                                            discountCount
                                        )
                                    )
                                )
                            }
                        }
                    }
            }
    }

    private fun getTransactions(
        startTime: DateTime,
        endTime: DateTime,
        businessId: String,
    ): Observable<List<Transaction>> {
        val startTimeInMilliSec = startTime.millis / 1000
        val endTimeInMilliSec = endTime.millis / 1000
        return Observable.combineLatest(
            transactionRepo.get().listTransactionsBetweenBillDate(startTimeInMilliSec, endTimeInMilliSec, businessId),
            collectionsAPI.get().listCollections(businessId),
            ::filterTransactions
        )
    }

    private fun filterTransactions(transactions: List<Transaction>, collections: List<Collection>): List<Transaction> {
        return transactions.filter {
            it.transactionState != Transaction.PROCESSING &&
                it.isDeleted.not() &&
                isCompleteOnlinePayment(it, collections)
        }
    }

    private fun isCompleteOnlinePayment(transaction: Transaction?, collections: List<Collection>): Boolean {
        if (transaction == null) return false

        if (transaction.isOnlinePaymentTransaction) {
            val collection = collections.firstOrNull { it.id == transaction.collectionId }
            return collection?.status == CollectionStatus.COMPLETE
        }

        return true
    }

    private fun process(
        customers: List<Customer>,
        transactions: List<Transaction>,
    ): Pair<List<TransactionWrapper>, Set<String>> {
        Timber.d("$TAG Processing ${transactions.size} Transactions and ${customers.size} Customers")
        val customerMap = HashMap<String, Customer>()
        for (customer in customers) {
            customerMap[customer.id] = customer
        }

        val txs = ArrayList<TransactionWrapper>()
        val missingCustomers = HashSet<String>()
        for (transaction in transactions) {
            val customer = customerMap[transaction.customerId]
            if (customer != null) {
                if (customer.state != Customer.State.BLOCKED) {
                    txs.add(
                        TransactionWrapper(
                            transaction,
                            getCustomerName(customer),
                            customer.status == 1
                        )
                    )
                }
            } else {
                missingCustomers.add(transaction.customerId)
                txs.add(
                    TransactionWrapper(
                        transaction, null,
                        false
                    )
                )
            }
        }
        Timber.d("$TAG Total transactions count =${txs.size} missingCustomers count ${missingCustomers.size}")
        return Pair(txs, missingCustomers)
    }

    private fun getCustomerName(customer: Customer): String {
        return if (customer.status == 1) {
            customer.description
        } else {
            try {
                customer.description.substring(0, customer.description.indexOf("" + " ["))
            } catch (e: Exception) {
                customer.description
            }
        }
    }

    data class Response(
        val transactionWrappers: List<TransactionWrapper>,
        val totalPaymentAmount: Long = 0,
        val totalPaymentCount: Int = 0,
        val totalCreditCount: Int = 0,
        var totalCreditAmount: Long = 0,
        var isShowLoadMore: Boolean = false,
        val totalDiscountAmount: Long,
        val totalDiscountCount: Int,
    )

    data class Request(
        val startTime: DateTime,
        val endTime: DateTime,
        val isShowAll: Boolean, // Show all transaction on UI. We Show only INITIAL_LOADING_COUNT transactions initially
    )
}
