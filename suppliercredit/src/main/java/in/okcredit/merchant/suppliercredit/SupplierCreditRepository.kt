package `in`.okcredit.merchant.suppliercredit

import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import merchant.okcredit.accounting.contract.HomeSortType
import merchant.okcredit.suppliercredit.contract.FlyweightSupplier
import org.joda.time.DateTime

interface SupplierCreditRepository {

    fun listActiveSuppliers(businessId: String): Observable<List<Supplier>>

    fun listActiveSuppliersByFlyweight(businessId: String): Flow<List<FlyweightSupplier>>

    fun getSupplierBalance(businessId: String): Observable<Long>

    fun listActiveSuppliersIds(businessId: String): Observable<List<String>>

    fun getSuppliersCount(businessId: String): Observable<Long>

    fun getSupplier(supplierId: String, businessId: String): Observable<Supplier>

    fun getActiveSuppliersCount(businessId: String): Flow<Long>

    fun getSuppliers(businessId: String): Observable<List<Supplier>>

    fun getSupplierByMobile(mobile: String, businessId: String): Single<Supplier>

    suspend fun getIsBlocked(businessId: String, supplierId: String): Boolean

    suspend fun getIsAddTransactionRestricted(businessId: String, supplierId: String): Boolean

    fun listActiveTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>>?

    fun listTransactions(supplierId: String, businessId: String): Observable<List<Transaction>>

    fun listTransactionsSortedByBillDate(supplierId: String, businessId: String): Observable<List<Transaction>>

    fun listDirtyTransactions(businessId: String): Observable<List<Transaction>>

    fun getTransaction(txnId: String, businessId: String): Observable<Transaction>

    // write operations
    fun addSupplier(name: String, mobile: String? = null, profileImage: String?, businessId: String): Single<Supplier>

    fun updateSuppler(
        supplier: Supplier,
        txnAlertChanged: Boolean,
        state: Int,
        updateState: Boolean,
        businessId: String,
    ): Completable

    fun deleteSupplier(supplierId: String, businessId: String): Completable

    fun addTransaction(
        transaction: Transaction,
        businessId: String,
    ): Completable

    fun syncTransaction(
        localTransaction: Transaction,
        businessId: String,
    ): Single<String>

    fun deleteTransaction(txnId: String, businessId: String): Completable

    fun markActivityAsSeen(supplierId: String): Completable

    fun setLastActivityTime(supplierId: String): Completable

    fun clearLocalData(): Completable

    fun reactivateSupplier(supplierId: String, supplierName: String? = null, businessId: String): Completable

    fun removeAllTransaction(supplierId: String): Completable

    fun syncSuppliers(businessId: String): Completable
    fun syncSpecificSupplier(supplierId: String, businessId: String): Completable
    fun syncEverything(businessId: String): Completable
    fun syncAllTransactions(startTime: DateTime? = null, businessId: String): Completable
    fun syncSupplierEnabledCustomerIds(businessId: String): Completable
    fun scheduleSyncSupplierEnabledCustomerIds(businessId: String): Completable
    fun executeSyncSupplierAndTransactions(supplierId: String, businessId: String): Completable
    fun listTransactions(supplierId: String, txnStartTime: Long, businessId: String): Observable<List<Transaction>>
    fun listSupplierTransactionsBetweenBillDate(
        supplierId: String,
        customerTxnStartTimeInMilliSec: Long,
        startTimeInMilliSec: Long,
        endTimeInMilliSec: Long,
        businessId: String,
    ): Observable<List<Transaction>>

    fun updateSupplerName(updatedName: String, supplierId: String): Completable

    fun getSortType(businessId: String): Observable<HomeSortType>

    fun setSortType(type: HomeSortType, businessId: String): Completable

    suspend fun getLatestTransactionCreateTimeOnSupplier(supplierId: String, businessId: String): DateTime

    fun syncNotificationReminder(businessId: String): Completable

    fun getNotificationReminderData(businessId: String): Single<List<NotificationReminderData>>

    fun createNotificationReminder(accountId: String, businessId: String): Single<Boolean>

    fun updateNotificationReminderById(notificationId: String, status: Int): Completable
}
