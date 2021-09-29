package `in`.okcredit.merchant.suppliercredit

import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderData
import `in`.okcredit.merchant.suppliercredit.store.database.NotificationReminder
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import merchant.okcredit.accounting.contract.HomeSortType
import merchant.okcredit.suppliercredit.contract.FlyweightSupplier
import org.joda.time.DateTime

interface SupplierLocalSource {

    fun listSuppliers(businessId: String): Observable<List<Supplier>>

    fun listActiveSuppliers(businessId: String): Observable<List<Supplier>>

    fun listActiveSuppliersByFlyweight(businessId: String): Flow<List<FlyweightSupplier>>

    fun listActiveSuppliersIds(businessId: String): Observable<List<String>>

    fun getSuppliersCount(businessId: String): Observable<Long>

    fun getActiveSuppliersCount(businessId: String): Flow<Long>

    fun getSupplier(supplierId: String, businessId: String): Observable<Supplier>

    fun getSuppliers(businessId: String): Observable<List<Supplier>>

    fun getSupplierByMobile(mobile: String, businessId: String): Single<Supplier>

    suspend fun getIsBlocked(businessId: String, supplierId: String): Boolean

    suspend fun getIsAddTransactionRestricted(businessId: String, supplierId: String): Boolean

    fun listTransactions(supplierId: String, businessId: String): Observable<List<Transaction>>

    fun listTransactionsSortedByBillDate(supplierId: String, businessId: String): Observable<List<Transaction>>

    fun listDirtyTransactions(businessId: String): Observable<List<Transaction>>

    fun getTransaction(txnId: String, businessId: String): Observable<Transaction>

    fun saveSupplier(supplier: Supplier, businessId: String): Completable

    fun saveSuppliers(suppliers: List<Supplier>, businessId: String): Completable

    fun saveTransaction(txn: Transaction, businessId: String): Completable

    fun saveTransactions(transactions: List<Transaction>, businessId: String): Completable

    fun setLastViewTime(supplierId: String, time: Long): Completable

    fun setLastActivityTime(supplierId: String, time: Long): Completable

    fun getLastSyncEverythingTime(): Observable<Pair<Boolean, DateTime?>>

    fun setLastSyncEverythingTime(time: DateTime?, businessId: String): Completable

    fun getSupplierBalance(businessId: String): Observable<Long>

    // once transaction (that is first created locally with random id) is sent to server , server returns same transaction with different 'id'
    // we remove the transaction that was sent to server which had local 'id' and once again save transaction sent by server to db
    fun removeTransaction(txnId: String): Completable

    fun removeAllTransaction(supplierId: String): Completable

    fun deleteSupplierTable(): Completable

    fun deleteTransactionTable(): Completable

    fun clearLastSyncEverythingTime(): Completable

    fun cancelWorker(): Completable

    fun clear(): Completable

    fun saveSupplierEnabledCustomerIds(it: List<String>, businessId: String): Completable

    fun updateSupplierName(updatedName: String, supplierId: String): Completable

    fun listTransactions(supplierId: String, txnStartTime: Long, businessId: String): Observable<List<Transaction>>

    fun listSupplierTransactionsBetweenBillDate(
        supplierId: String,
        customerTxnStartTimeInMilliSec: Long,
        startTimeInMilliSec: Long,
        endTimeInMilliSec: Long,
        businessId: String,
    ): Observable<List<Transaction>>

    fun saveRestrictTxnEnabledCustomers(list: List<String>, businessId: String): Completable

    fun saveAccountIdsListWithAddTransactionRestricted(
        accountIdListWithAddTransactionRestricted: List<String>,
        businessId: String
    ): Completable

    fun listActiveTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Flowable<List<`in`.okcredit.merchant.suppliercredit.store.database.Transaction?>?>?

    fun getSortType(businessId: String): Observable<HomeSortType>

    fun setSortType(type: HomeSortType, businessId: String): Completable

    fun saveNotificationReminder(notificationReminder: List<NotificationReminder>): Completable

    fun getStartTimeNotificationReminder(businessId: String): Single<String>

    fun getNotificationReminder(businessId: String): Single<List<NotificationReminderData>>

    fun getProcessedNotificationReminder(businessId: String): Single<List<NotificationReminder>>

    fun deleteProcessedNotificationReminder(notificationReminder: List<NotificationReminder>): Completable

    fun updateNotificationReminderStatusById(notificationId: String, status: Int): Completable

    suspend fun getLatestTransactionCreateTimeOnSupplier(supplierId: String, businessId: String): DateTime
}
