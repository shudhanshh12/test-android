package `in`.okcredit.backend.collection_usecases

import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection.BILL_DATE
import `in`.okcredit.merchant.suppliercredit.use_case.GetSupplierScreenSortSelection.SupplierScreenSortSelection.CREATE_DATE
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.functions.Function3
import org.joda.time.DateTime
import timber.log.Timber
import javax.inject.Inject

/**
 *  This class is used to fetch supplier and transactions of suppliers
 */

class GetSupplierStatement @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    data class TransactionWrapper(
        val transaction: Transaction,
        val currentDue: Long,
        val collection: Collection? = null,
    )

    data class Response(
        val supplier: Supplier,
        val transactions: List<TransactionWrapper>,
        val lastIndexOfZeroBalanceDue: Int = 0,
    )

    fun execute(supplierId: String, sortSelection: SupplierScreenSortSelection): Observable<Result<Response>> {

        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                Observable.combineLatest(
                    markActivitySeenObservable(supplierId, businessId),
                    supplierTxnsObservable(supplierId, businessId, sortSelection),
                    supplierCollectionsObservable(supplierId, businessId).startWith(mutableListOf<Collection>()),
                    Function3<Supplier, List<Transaction>, List<Collection>, Response> { supplier, transactions, collections ->

                        val txnPair = getTxnWithDue(transactions, collections)

                        return@Function3 Response(
                            supplier,
                            txnPair.second,
                            txnPair.first
                        )
                    }
                )
            }

        )
    }

    private fun getTxnWithDue(
        transactions: List<Transaction>,
        collections: List<Collection>,
    ): Pair<Int, ArrayList<TransactionWrapper>> {
        val transactionsWrapper = arrayListOf<TransactionWrapper>()
        var currentDue = 0L
        var lastIndexOfZeroBalanceDue = 0
        transactions.reversed().mapIndexed { index, txn ->
            // commenting for now when design done can use
            if (txn.transactionState == Transaction.Constants.PROCESSING) {
                currentDue = currentDue
            } else if (txn.payment) {
                if (txn.deleted) {
                    currentDue = currentDue
                } else {
                    currentDue -= txn.amount
                }
            } else {
                if (txn.deleted) {
                    currentDue = currentDue
                } else {
                    currentDue += txn.amount
                }
            }
            if (currentDue == 0L) Timber.i("current due= $currentDue")
            if (currentDue == 0L &&
                !txn.deleted &&
                index != transactions.size - 1
            ) {
                lastIndexOfZeroBalanceDue = index
            }
            transactionsWrapper.add(
                TransactionWrapper(
                    txn,
                    currentDue,
                    collections.find { collection -> collection.id == txn.collectionId }
                )
            )
        }
        return Pair(lastIndexOfZeroBalanceDue, transactionsWrapper)
    }

    private fun supplierCollectionsObservable(supplierId: String, businessId: String): Observable<List<Collection>> {
        collectionSyncer.get().scheduleSyncCollections(
            syncType = CollectionSyncer.SYNC_SUPPLIER_COLLECTIONS,
            source = CollectionSyncer.Source.SUPPLIER_STATEMENT,
            businessId = businessId
        )
        return collectionRepository.get().getCollectionsOfCustomerOrSupplier(supplierId, businessId)
    }

    private fun supplierTxnsObservable(
        supplierId: String,
        businessId: String,
        sortSelection: SupplierScreenSortSelection
    ): Observable<List<Transaction>> {
        return when (sortSelection) {
            CREATE_DATE -> supplierCreditRepository.get().listTransactions(supplierId, businessId)
            BILL_DATE -> supplierCreditRepository.get().listTransactionsSortedByBillDate(supplierId, businessId)
        }
    }

    private fun markActivitySeenObservable(supplierId: String, businessId: String): Observable<Supplier> {
        return supplierCreditRepository.get().markActivityAsSeen(supplierId)
            .andThen(
                supplierCreditRepository.get().getSupplier(supplierId, businessId)
            )
    }

    data class Request(
        val startTime: DateTime,
        val endTime: DateTime,
        val isShowAll: Boolean, // Show all transaction on UI. We Show only INITIAL_LOADING_COUNT transactions initially
    )
}
