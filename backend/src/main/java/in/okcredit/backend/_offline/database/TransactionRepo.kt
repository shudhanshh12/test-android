package `in`.okcredit.backend._offline.database

import `in`.okcredit.backend._offline.common.CoreModuleMapper
import `in`.okcredit.backend._offline.common.Utils
import `in`.okcredit.backend._offline.database.internal.DbEntities
import `in`.okcredit.backend._offline.database.internal.DbEntityMapper
import `in`.okcredit.backend._offline.database.internal.DbEntityMapper.convertTransactionImageListToTransactionImageString
import `in`.okcredit.backend._offline.database.internal.TransactionDao
import `in`.okcredit.merchant.core.CoreSdk
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.model.TransactionImage
import tech.okcredit.android.base.crashlytics.RecordException.recordException
import tech.okcredit.android.base.utils.ImageCache
import tech.okcredit.android.base.utils.ThreadUtils.database
import tech.okcredit.android.base.utils.ThreadUtils.worker
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.ArrayList

class TransactionRepo @Inject constructor(
    private val transactionDao: Lazy<TransactionDao>,
    private val coreSdk: Lazy<CoreSdk>,
    private val imageCache: Lazy<ImageCache>,
) {
    fun listDirtyTransactions(customerId: String?, businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable { it: Boolean ->
                if (it) {
                    return@flatMapObservable coreListDirtyTransactions(customerId, businessId)
                } else {
                    return@flatMapObservable backendListDirtyTransactions(customerId, businessId)
                }
            }
    }

    private fun coreListDirtyTransactions(customerId: String?, businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .listDirtyTransactions(true, businessId)
            .map { coreTransactionList: List<`in`.okcredit.merchant.core.model.Transaction> ->
                val transactionList: MutableList<Transaction> = ArrayList()
                for (coreTransaction in coreTransactionList) {
                    transactionList.add(
                        CoreModuleMapper.toTransaction(coreTransaction)
                    )
                }
                transactionList
            }
            .map { transactions: List<Transaction> ->
                if (customerId == null || customerId.isEmpty()) {
                    return@map transactions
                } else {
                    val filtered: MutableList<Transaction> = ArrayList()
                    for (transaction in transactions) {
                        if (transaction.customerId == customerId) {
                            filtered.add(transaction)
                        }
                    }
                    return@map filtered
                }
            }
    }

    private fun backendListDirtyTransactions(customerId: String?, businessId: String): Observable<List<Transaction>> {
        return transactionDao
            .get()
            .listDirtyTransactions(true, businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction> ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
            .map { transactions: List<Transaction> ->
                if (customerId == null || customerId.isEmpty()) {
                    return@map transactions
                } else {
                    val filtered: MutableList<Transaction> = ArrayList()
                    for (transaction in transactions) {
                        if (transaction.customerId == customerId) {
                            filtered.add(transaction)
                        }
                    }
                    return@map filtered
                }
            }
            .toObservable()
    }

    fun listTransactions(customerId: String, businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreListTransactions(customerId, businessId)
                } else {
                    return@flatMapObservable backendListTransactions(customerId, businessId)
                }
            }
    }

    private fun coreListTransactions(customerId: String, businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .listTransactions(customerId, businessId)
            .map { coreTransactionList: List<`in`.okcredit.merchant.core.model.Transaction> ->
                val transactionList: MutableList<Transaction> = ArrayList()
                for (coreTransaction in coreTransactionList) {
                    transactionList.add(
                        CoreModuleMapper.toTransaction(coreTransaction)
                    )
                }
                transactionList
            }
    }

    private fun backendListTransactions(customerId: String, businessId: String): Observable<List<Transaction>> {
        return transactionDao
            .get()
            .listTransactions(customerId, businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction> ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
            .toObservable()
    }

    fun listNonDeletedTransactionsByBillDate(customerId: String, businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreListNonDeletedTransactionsByBillDate(customerId, businessId)
                } else {
                    return@flatMapObservable backendListNonDeletedTransactionsByBillDate(customerId, businessId)
                }
            }
    }

    private fun coreListNonDeletedTransactionsByBillDate(
        customerId: String,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreSdk.get()
            .listNonDeletedTransactionsByBillDate(customerId, businessId)
            .map { coreTransactionList -> coreTransactionList.map { txn -> CoreModuleMapper.toTransaction(txn) } }
    }

    private fun backendListNonDeletedTransactionsByBillDate(
        customerId: String,
        businessId: String,
    ): Observable<List<Transaction>> {
        return transactionDao
            .get()
            .listNonDeletedTransactionsByBillDate(customerId, businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction> ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
    }

    fun listTransactions(customerId: String, txnStartTime: Long, businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreListTransactions(customerId, txnStartTime, businessId)
                } else {
                    return@flatMapObservable backendListTransactions(customerId, txnStartTime, businessId)
                }
            }
            .map { setupImageUrlsInTransactionList(it) }
    }

    private fun coreListTransactions(
        customerId: String,
        txnStartTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreSdk.get()
            .listTransactions(customerId, txnStartTime, businessId)
            .map { coreTransactionList -> coreTransactionList.map { txn -> CoreModuleMapper.toTransaction(txn) } }
    }

    private fun backendListTransactions(
        customerId: String,
        txnStartTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return transactionDao
            .get()
            .listTransactions(customerId, txnStartTime, businessId)
            .doOnError { throwable: Throwable? ->
                recordException(
                    throwable!!
                )
            }
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction>? ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
            .toObservable()
    }

    private fun setupImageUrlsInTransactionList(list: List<Transaction>): List<Transaction> {
        return list.map { txn ->
            val finalTxnImageList = mutableListOf<TransactionImage>()
            txn.receiptUrl?.let { receiptUrl ->
                for (txnImg in receiptUrl) {
                    finalTxnImageList.add(txnImg.copy(imageUrl = imageCache.get().getImage(txnImg.url)))
                }
            }
            txn.copy(receiptUrl = if (finalTxnImageList.isEmpty()) null else finalTxnImageList)
        }
    }

    fun listTransactionsSortedByBillDate(
        customerId: String,
        txnStartTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreListTransactionsSortedByBillDate(customerId, txnStartTime, businessId)
                } else {
                    return@flatMapObservable backendListTransactionsSortedByBillDate(
                        customerId,
                        txnStartTime,
                        businessId
                    )
                }
            }
            .map { setupImageUrlsInTransactionList(it) }
    }

    private fun coreListTransactionsSortedByBillDate(
        customerId: String,
        txnStartTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreSdk.get()
            .listTransactionsSortedByBillDate(customerId, txnStartTime, businessId)
            .map { coreTransactionList -> coreTransactionList.map { txn -> CoreModuleMapper.toTransaction(txn) } }
    }

    private fun backendListTransactionsSortedByBillDate(
        customerId: String,
        txnStartTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return transactionDao
            .get()
            .listTransactionsSortedByBillDate(customerId, txnStartTime, businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction>? ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
            .toObservable()
    }

    fun listTransactions(businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable { it: Boolean ->
                if (it) {
                    return@flatMapObservable coreListTransactions(businessId)
                } else {
                    return@flatMapObservable backendListTransactions(businessId)
                }
            }
    }

    private fun coreListTransactions(businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .listTransactions(businessId)
            .map { coreTransactionList: List<`in`.okcredit.merchant.core.model.Transaction> ->
                val transactionList: MutableList<Transaction> = ArrayList()
                for (coreTransaction in coreTransactionList) {
                    transactionList.add(
                        CoreModuleMapper.toTransaction(coreTransaction)
                    )
                }
                transactionList
            }
    }

    private fun backendListTransactions(businessId: String): Observable<List<Transaction>> {
        return transactionDao
            .get()
            .listTransactions(businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction> ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
            .toObservable()
    }

    fun listTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreListTransactionsBetweenBillDate(startTime, endTime, businessId)
                } else {
                    return@flatMapObservable backendListTransactionsBetweenBillDate(startTime, endTime, businessId)
                }
            }
    }

    private fun coreListTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreSdk.get()
            .listActiveTransactionsBetweenBillDate(startTime, endTime, businessId)
            .map { coreTransactionList: List<`in`.okcredit.merchant.core.model.Transaction> ->
                val transactionList: MutableList<Transaction> = ArrayList()
                for (coreTransaction in coreTransactionList) {
                    transactionList.add(
                        CoreModuleMapper.toTransaction(coreTransaction)
                    )
                }
                transactionList
            }
    }

    private fun backendListTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return transactionDao
            .get()
            .listActiveTransactionsBetweenBillDate(startTime, endTime, businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction> ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
            .toObservable()
    }

    // -------------------List of customer Txn between specified date range ------------------------
    // ---------------------------------------------------------------------------------------------
    fun listCustomerTransactionsBetweenBillDate(
        customerId: String,
        customerTxnTime: Long,
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreCustomerListTransactionsBetweenBillDate(
                        customerId, customerTxnTime, startTime, endTime, businessId
                    )
                } else {
                    return@flatMapObservable backendCustomerListTransactionsBetweenBillDate(
                        customerId, customerTxnTime, startTime, endTime, businessId
                    )
                }
            }
    }

    private fun coreCustomerListTransactionsBetweenBillDate(
        customerId: String,
        customerTxnTime: Long,
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreSdk.get()
            .listActiveTransactionsBetweenBillDate(
                customerId, customerTxnTime, startTime, endTime, businessId
            )
            .map { coreTransactionList: List<`in`.okcredit.merchant.core.model.Transaction> ->
                val transactionList: MutableList<Transaction> = ArrayList()
                for (coreTransaction in coreTransactionList) {
                    transactionList.add(
                        CoreModuleMapper.toTransaction(coreTransaction)
                    )
                }
                transactionList
            }
    }

    private fun backendCustomerListTransactionsBetweenBillDate(
        customerId: String,
        customerTxnTime: Long,
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return transactionDao
            .get()
            .listCustomerActiveTransactionsBetweenBillDate(
                customerId, customerTxnTime, startTime, endTime, businessId
            )
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction>? ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    fun putTransactionsV2(transactions: List<DbEntities.Transaction?>): Completable {
        return Completable.fromAction {
            val list = transactions.toTypedArray()
            transactionDao.get().insertTransaction(*list)
        }
            .doOnError { it: Throwable ->
                Timber.d("<<<<FileDownload  A Error: %s", it.toString())
                recordException(it)
            }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun putTransactionsWithIgnoreStrategy(
        transactions: List<DbEntities.Transaction?>,
    ): Completable {
        return Completable.fromAction {
            val list = transactions.toTypedArray()
            transactionDao.get().insertTransactionWithIgnoreConflictStrategy(*list)
        }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun putTransaction(transaction: Transaction, businessId: String): Completable {
        return Completable.fromAction {
            transactionDao
                .get()
                .insertTransaction(
                    DbEntityMapper.TRANSACTION(businessId).convert(transaction)
                )
        }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun putTransactionV2(transaction: DbEntities.Transaction?): Completable {
        return Completable.fromAction { transactionDao.get().insertTransaction(transaction) }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun clear(businessId: String? = null): Completable {
        Timber.e("<<<<SyncAuthScope Clear Clear Clear Clear")
        return Completable.fromAction {
            if (businessId == null) {
                transactionDao.get().deleteAllTransactions()
            } else {
                transactionDao.get().clearTransactions(businessId)
            }
        }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun isTransactionPresent(txnId: String, businessId: String): Single<Boolean> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMap { it: Boolean ->
                if (it) {
                    return@flatMap coreIsTransactionPresent(txnId)
                } else {
                    return@flatMap backendIsTransactionPresent(txnId)
                }
            }
    }

    private fun coreIsTransactionPresent(txnId: String): Single<Boolean> {
        return coreSdk.get().isTransactionPresent(txnId)
    }

    private fun backendIsTransactionPresent(txnId: String): Single<Boolean> {
        return transactionDao
            .get()
            .isTransactionPresent(txnId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { count: Int -> count == 1 }
    }

    fun getTransaction(txnId: String, businessId: String): Observable<Transaction> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreGetTransaction(txnId, businessId)
                } else {
                    return@flatMapObservable backendGetTransaction(txnId, businessId)
                }
            }
            .map {
                val finalTxnImageList = mutableListOf<TransactionImage>()
                it.receiptUrl?.let { receiptUrl ->
                    for (txnImg in receiptUrl) {
                        finalTxnImageList.add(txnImg.copy(imageUrl = imageCache.get().getImage(txnImg.url)))
                    }
                }

                it.copy(receiptUrl = if (finalTxnImageList.isEmpty()) null else finalTxnImageList)
            }
    }

    private fun coreGetTransaction(txnId: String, businessId: String): Observable<Transaction> {
        return coreSdk.get().getTransaction(txnId, businessId)
            .map {
                CoreModuleMapper.toTransaction(it)
            }
    }

    private fun backendGetTransaction(txnId: String, businessId: String): Observable<Transaction> {
        return transactionDao
            .get()
            .getTransaction(txnId)
            .subscribeOn(database())
            .observeOn(worker())
            .map<Transaction> { transaction: DbEntities.Transaction ->
                DbEntityMapper.TRANSACTION(businessId).reverse().convert(transaction)
            }
            .toObservable()
    }

    fun getTransactionIdFromCollection(collectionId: String, businessId: String): Single<String> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMap {
                if (it) {
                    return@flatMap coreGetTransactionIdFromCollection(collectionId, businessId)
                } else {
                    return@flatMap backendGetTransactionIdFromCollection(collectionId, businessId)
                }
            }
    }

    private fun coreGetTransactionIdFromCollection(collectionId: String, businessId: String): Single<String> {
        return coreSdk.get().getTransactionIdForCollection(collectionId, businessId)
    }

    private fun backendGetTransactionIdFromCollection(collectionId: String, businessId: String): Single<String> {
        return transactionDao.get()
            .getTransactionIdForCollectionId(collectionId, businessId)
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun isTransactionForCollectionPresent(collectionId: String, businessId: String): Single<Boolean> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMap {
                if (it) {
                    return@flatMap coreIsTransactionForCollectionPresent(collectionId, businessId)
                } else {
                    return@flatMap backendIsTransactionForCollectionPresent(collectionId, businessId)
                }
            }
    }

    private fun coreIsTransactionForCollectionPresent(collectionId: String, businessId: String): Single<Boolean> {
        return coreSdk.get().isTransactionForCollectionPresent(collectionId, businessId)
    }

    private fun backendIsTransactionForCollectionPresent(collectionId: String, businessId: String): Single<Boolean> {
        return transactionDao
            .get()
            .getTransactionsCountForCollectionId(collectionId, businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { count: Int -> count == 1 }
    }

    fun getTransactionUsingCollectionId(collectionId: String, businessId: String): Observable<Transaction> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreGetTransactionUsingCollectionId(collectionId, businessId)
                } else {
                    return@flatMapObservable backendGetTransactionUsingCollectionId(collectionId, businessId)
                }
            }
    }

    private fun coreGetTransactionUsingCollectionId(collectionId: String, businessId: String): Observable<Transaction> {
        return coreSdk.get()
            .getTransactionByCollectionId(collectionId, businessId)
            .map {
                CoreModuleMapper.toTransaction(it)
            }
    }

    private fun backendGetTransactionUsingCollectionId(
        collectionId: String,
        businessId: String,
    ): Observable<Transaction> {
        return transactionDao
            .get()
            .getTransactionUsingCollectionId(collectionId, businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map<Transaction> { transaction: DbEntities.Transaction? ->
                DbEntityMapper.TRANSACTION(businessId).reverse().convert(transaction)
            }
            .toObservable()
    }

    fun markSmsSent(txnId: String, businessId: String): Completable {
        return getTransaction(txnId, businessId)
            .map { txn: Transaction -> txn.withSmsSent(true) }
            .switchMapCompletable { txn: Transaction -> putTransaction(txn, businessId) }
    }

    fun updateTransactionImage(
        selectedImages: ArrayList<TransactionImage>,
        transactionId: String,
    ): Completable {
        return Completable.fromAction {
            val jsonString = convertTransactionImageListToTransactionImageString(
                selectedImages
            )
            transactionDao.get().updateTransactionImage(jsonString, transactionId)
        }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun updateTransactionNote(note: String, transactionId: String): Completable {
        return Completable.fromAction { transactionDao.get().updateTransactionNote(note, transactionId) }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun updateTransactionAmount(
        amount: Long,
        transactionId: String,
    ): Completable {
        return Completable.fromAction { transactionDao.get().updateTransactionAmount(amount, transactionId) }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun lastUpdatedTransactionTime(businessId: String): Single<Long> {
        return transactionDao
            .get()
            .lastUpdatedTransaction(businessId)
            .onErrorReturn { 0L } // return 0 on Exception(EmptyResultSetException)
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun replaceTransaction(oldTxnId: String?, transaction: Transaction?, businessId: String): Completable {
        return Completable.fromAction {
            transactionDao
                .get()
                .replaceTransaction(
                    oldTxnId,
                    DbEntityMapper.TRANSACTION(businessId).convert(transaction)
                )
        }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun allTransactionsCount(businessId: String): Single<Int> = coreSdk.get()
        .isCoreSdkFeatureEnabled(businessId)
        .flatMap {
            if (it) {
                return@flatMap coreGetAllTransactionsCount(businessId)
            } else {
                return@flatMap backendGetAllTransactionsCount(businessId)
            }
        }

    fun getTransactionCountByType(type: Int, businessId: String): Single<Int> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMap {
                if (it) {
                    return@flatMap coreGetAllTransactionsCountByType(type, businessId)
                } else {
                    return@flatMap backendGetAllTransactionsCountByType(type, businessId)
                }
            }
    }

    private fun backendGetAllTransactionsCountByType(type: Int, businessId: String): Single<Int> {
        return transactionDao
            .get()
            .getAllTransactionsCountByType(type, businessId)
            .subscribeOn(database())
            .observeOn(worker())
    }

    private fun coreGetAllTransactionsCount(businessId: String): Single<Int> {
        return coreSdk.get().getAllTransactionsCount(businessId)
    }

    private fun coreGetAllTransactionsCountByType(type: Int, businessId: String): Single<Int> {
        return coreSdk.get().getTransactionCountByType(type, businessId)
    }

    private fun backendGetAllTransactionsCount(businessId: String): Single<Int> {
        return transactionDao
            .get()
            .getAllTransactionsCount(businessId)
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun deleteAllTransactionForCustomer(customerId: String?, businessId: String): Completable {
        return Completable.fromAction { transactionDao.get().deleteAllTransactionsForCustomer(customerId, businessId) }
            .subscribeOn(database())
            .observeOn(worker())
    }

    fun getFirstTransaction(businessId: String): Single<Transaction> = coreSdk.get()
        .isCoreSdkFeatureEnabled(businessId)
        .flatMap {
            if (it) {
                return@flatMap coreGetFirstTransaction(businessId)
            } else {
                return@flatMap backendGetFirstTransaction(businessId)
            }
        }

    private fun coreGetFirstTransaction(businessId: String): Single<Transaction> {
        return coreSdk.get().getFirstTransaction(businessId)
            .map {
                CoreModuleMapper.toTransaction(it)
            }
    }

    private fun backendGetFirstTransaction(businessId: String): Single<Transaction> {
        return transactionDao
            .get()
            .getFirstTransaction(businessId)
            .map { transaction: DbEntities.Transaction? ->
                DbEntityMapper.TRANSACTION(businessId).reverse().convert(transaction)
            }
    }

    fun getLastTransaction(businessId: String): Single<Transaction> = coreSdk.get()
        .isCoreSdkFeatureEnabled(businessId)
        .flatMap {
            if (it) {
                return@flatMap coreGetLastTransaction(businessId)
            } else {
                return@flatMap backendGetLastTransaction(businessId)
            }
        }

    private fun coreGetLastTransaction(businessId: String): Single<Transaction> {
        return coreSdk.get().getLastTransaction(businessId)
            .map {
                CoreModuleMapper.toTransaction(it)
            }
    }

    private fun backendGetLastTransaction(businessId: String): Single<Transaction> {
        return transactionDao
            .get()
            .getLastTransaction(businessId)
            .map { transaction: DbEntities.Transaction? ->
                DbEntityMapper.TRANSACTION(businessId).reverse().convert(transaction)
            }
    }

    fun getLatestPaymentAddedByCustomer(customerId: String, businessId: String): Single<Transaction> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMap {
                if (it) {
                    return@flatMap coreGetLatestTransactionAddedByCustomer(customerId, businessId)
                } else {
                    return@flatMap backendGetLatestTransactionAddedByCustomer(customerId, businessId)
                }
            }
    }

    private fun backendGetLatestTransactionAddedByCustomer(
        customerId: String,
        businessId: String,
    ): Single<Transaction> {
        return transactionDao
            .get()
            .getLatestPaymentAddedByCustomer(customerId, businessId)
            .map { transaction: DbEntities.Transaction? ->
                DbEntityMapper.TRANSACTION(businessId).reverse().convert(transaction)
            }
    }

    private fun coreGetLatestTransactionAddedByCustomer(customerId: String, businessId: String): Single<Transaction> {
        return coreSdk.get()
            .getLatestTransactionAddedByCustomer(customerId, businessId)
            .map {
                CoreModuleMapper.toTransaction(it)
            }
    }

    fun getNumberOfSyncedTransactionsTillGivenUpdatedTime(lastUpdatedTime: Long, businessId: String): Single<Int> {
        return transactionDao
            .get()
            .getNumberOfSyncedTransactionsTillGivenUpdatedTime(lastUpdatedTime, businessId)
    }

    fun listOnlineTransactions(customerId: String, businessId: String): Observable<List<Transaction>> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMapObservable {
                if (it) {
                    return@flatMapObservable coreListOnlineTransactions(customerId)
                } else {
                    return@flatMapObservable backendListOnlineTransactions(customerId, businessId)
                }
            }
    }

    private fun backendListOnlineTransactions(
        customerId: String,
        businessId: String,
    ): ObservableSource<List<Transaction>>? {
        return transactionDao
            .get()
            .listOnlineTransactions(customerId)
            .subscribeOn(database())
            .observeOn(worker())
            .map { transactions: List<DbEntities.Transaction> ->
                Utils.mapList(
                    transactions,
                    DbEntityMapper.TRANSACTION(businessId).reverse()
                )
            }
            .toObservable()
            .startWith(emptyList<Transaction>())
    }

    private fun coreListOnlineTransactions(customerId: String): ObservableSource<List<Transaction>>? {
        return coreSdk.get().listOnlineTransactions(customerId)
            .map { coreTransactionList: List<`in`.okcredit.merchant.core.model.Transaction> ->
                val transactionList = mutableListOf<Transaction>()
                for (coreTransaction in coreTransactionList) {
                    transactionList.add(
                        CoreModuleMapper.toTransaction(coreTransaction)
                    )
                }
                transactionList.toList()
            }.startWith(emptyList<Transaction>())
    }

    fun getLatestTransactionCustomer(customerId: String, businessId: String): Single<Transaction> {
        return coreSdk.get()
            .isCoreSdkFeatureEnabled(businessId)
            .flatMap {
                if (it) {
                    return@flatMap coreGetLatestTransactionCustomer(customerId)
                } else {
                    return@flatMap backendGetLatestTransactionCustomer(customerId, businessId)
                }
            }
    }

    private fun backendGetLatestTransactionCustomer(customerId: String, businessId: String): Single<Transaction> {
        return transactionDao
            .get()
            .getLatestTransaction(customerId)
            .map { transaction: DbEntities.Transaction? ->
                DbEntityMapper.TRANSACTION(businessId).reverse().convert(transaction)
            }
    }

    private fun coreGetLatestTransactionCustomer(customerId: String): Single<Transaction> {
        return coreSdk.get()
            .getLatestTransaction(customerId)
            .map {
                CoreModuleMapper.toTransaction(it)
            }
    }

    fun deleteTransaction(transactionId: String) =
        Completable.fromAction { transactionDao.get().deleteTransaction(transactionId) }
}
