package `in`.okcredit.supplier.statement.usecase

import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.ISyncer
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.supplier.usecase.SupplierTransactionWrapper
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.DateTime
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class GetSupplierAccountSummary @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val sync: Lazy<ISyncer>,
    private val collectionAPI: Lazy<CollectionRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    companion object {
        const val TAG = "Tag"
        const val INITIAL_LOADING_COUNT = 1500
    }

    fun execute(request: SupplierRequest): Observable<Response> {
        Timber.d("$TAG started Executing txn between $request.startTime and $request.endTime")
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Observable
                .combineLatest<List<Transaction>, List<Supplier>, Pair<List<SupplierTransactionWrapper>, Set<String>>>(
                    getTransactions(request.startTime, request.endTime, businessId),
                    supplierCreditRepository.get().listActiveSuppliers(businessId),
                    { transactions, supplier -> process(supplier, transactions) }
                )
                .flatMap { res ->
                    var syncMissingSupplier = Completable.complete()
                    val missingSupplier = res.second
                    for (supplierId in missingSupplier) {
                        syncMissingSupplier = syncMissingSupplier.andThen(
                            sync.get().syncSupplier(supplierId, businessId)
                        )
                    }
                    var paymentAmount = 0L
                    var paymentCount = 0
                    var creditCount = 0
                    var creditAmount = 0L

                    for (transaction in res.first) {
                        if (transaction.transaction.transactionState != Transaction.Constants.PROCESSING) {
                            if (!transaction.transaction.payment) {
                                creditCount++
                                creditAmount += transaction.transaction.amount
                            } else if ((transaction.transaction.payment) || transaction.transaction.transactionState == Transaction.Constants.RETURN) {
                                paymentCount++
                                paymentAmount += transaction.transaction.amount
                            }
                        }
                    }
                    if (request.isShowAll) {
                        syncMissingSupplier.andThen(
                            Observable.just(
                                Response(
                                    res.first,
                                    paymentAmount,
                                    paymentCount,
                                    creditCount,
                                    creditAmount,
                                    false,
                                )
                            )
                        )
                    } else {
                        if (res.first.size > INITIAL_LOADING_COUNT) {
                            syncMissingSupplier.andThen(
                                Observable.just(
                                    Response(
                                        res.first.subList(0, INITIAL_LOADING_COUNT - 1),
                                        paymentAmount,
                                        paymentCount,
                                        creditCount,
                                        creditAmount,
                                        true,
                                    )
                                )
                            )
                        } else {
                            syncMissingSupplier.andThen(
                                Observable.just(
                                    Response(
                                        res.first,
                                        paymentAmount,
                                        paymentCount,
                                        creditCount,
                                        creditAmount,
                                        false,
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
            collectionAPI.get().listCollections(businessId),
            supplierCreditRepository.get()
                .listActiveTransactionsBetweenBillDate(startTimeInMilliSec, endTimeInMilliSec, businessId),
            ::filterTransactions
        )
    }

    private fun filterTransactions(collections: List<Collection>, transactions: List<Transaction>): List<Transaction> {
        return transactions.filter {
            it.transactionState != Transaction.Constants.PROCESSING &&
                it.deleted.not() &&
                isCompleteOnlinePayment(it, collections)
        }
    }

    private fun isCompleteOnlinePayment(transaction: Transaction?, collections: List<Collection>): Boolean {
        if (transaction == null) return false

        if (transaction.isOnlineTransaction()) {
            val collection = collections.firstOrNull { it.id == transaction.collectionId }
            return collection?.status == CollectionStatus.COMPLETE
        }

        return true
    }

    private fun process(
        supplierList: List<Supplier>,
        transactions: List<Transaction>,
    ): Pair<List<SupplierTransactionWrapper>, Set<String>> {
        Timber.d("$TAG Processing ${transactions.size} Transactions and ${supplierList.size} supplier")
        val supplierMap = HashMap<String, Supplier>()
        for (suppliers in supplierList) {
            supplierMap[suppliers.id] = suppliers
        }

        val txs = ArrayList<SupplierTransactionWrapper>()
        val missingSuppliers = HashSet<String>()
        for (transaction in transactions) {
            val supplier = supplierMap[transaction.supplierId]
            if (supplier != null) {
                if (supplier.state != Supplier.BLOCKED) {
                    txs.add(
                        SupplierTransactionWrapper(
                            transaction,
                            getSupplierName(supplier),
                            supplier.state == 1
                        )
                    )
                }
            } else {
                missingSuppliers.add(transaction.supplierId)
                txs.add(
                    SupplierTransactionWrapper(
                        transaction, null,
                        false
                    )
                )
            }
        }
        Timber.d("$TAG Total transactions count =${txs.size} missingCustomers count ${missingSuppliers.size}")
        return Pair(txs, missingSuppliers)
    }

    private fun getSupplierName(supplier: Supplier): String? {
        return if (supplier.state == 1) {
            supplier.name
        } else {
            try {
                supplier.name.substring(0, supplier.name.indexOf("" + " ["))
            } catch (e: Exception) {
                supplier.name
            }
        }
    }

    data class Response(
        val supplierTransactionWrappers: List<SupplierTransactionWrapper>,
        val totalPaymentAmount: Long = 0,
        val totalPaymentCount: Int = 0,
        val totalCreditCount: Int = 0,
        var totalCreditAmount: Long = 0,
        var showLoadMore: Boolean = false,
    )
}

data class SupplierSummary(val balance: Long?)

data class SupplierRequest(
    val startTime: DateTime,
    val endTime: DateTime,
    val isShowAll: Boolean, // Show all transaction on UI. We Show only INITIAL_LOADING_COUNT transactions initially
)
