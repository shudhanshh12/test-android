package `in`.okcredit.merchant.suppliercredit.store

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierLocalSource
import `in`.okcredit.merchant.suppliercredit.SyncerImpl
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderData
import `in`.okcredit.merchant.suppliercredit.server.internal.ApiEntityMapper
import `in`.okcredit.merchant.suppliercredit.store.SupplierPreferences.Keys.PREF_BUSINESS_LAST_SYNC_EVERYTHING_TIME
import `in`.okcredit.merchant.suppliercredit.store.SupplierPreferences.Keys.PREF_BUSINESS_SORT_TYPE
import `in`.okcredit.merchant.suppliercredit.store.database.DbEntityMapper
import `in`.okcredit.merchant.suppliercredit.store.database.NotificationReminder
import `in`.okcredit.merchant.suppliercredit.store.database.SupplierDataBaseDao
import `in`.okcredit.merchant.suppliercredit.use_case.ClearLastSyncEverythingTimeForAllBusinesses
import `in`.okcredit.merchant.suppliercredit.utils.CommonUtils
import `in`.okcredit.merchant.suppliercredit.utils.Utils
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.utils.CommonUtils.currentDateTime
import `in`.okcredit.shared.utils.TimeUtils.toSeconds
import android.content.SharedPreferences
import androidx.room.EmptyResultSetException
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.accounting.contract.HomeSortType
import merchant.okcredit.suppliercredit.contract.FlyweightSupplier
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_SC_ENABLED_CUSTOMERS
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.blockingGetLong
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ImageCache
import tech.okcredit.android.base.utils.ThreadUtils.database
import tech.okcredit.android.base.utils.ThreadUtils.worker
import tech.okcredit.android.base.workmanager.OkcWorkManager
import timber.log.Timber
import javax.inject.Inject
import `in`.okcredit.merchant.suppliercredit.store.database.Transaction as DbTransaction

class StoreImpl @Inject constructor(
    private val supplierDao: Lazy<SupplierDataBaseDao>,
    private val workManager: Lazy<OkcWorkManager>,
    private val keyValService: Lazy<KeyValService>,
    private val preferences: Lazy<SupplierPreferences>,
    private val imageCache: Lazy<ImageCache>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val clearLastSyncEverythingTimeForAllBusinesses: Lazy<ClearLastSyncEverythingTimeForAllBusinesses>,
) : SupplierLocalSource, SharedPreferences.OnSharedPreferenceChangeListener {

    private val lastSyncEverythingTime = BehaviorSubject.create<Pair<Boolean, DateTime?>>()

    init {
        rxCompletable {
            // set value initially
            val businessId = getActiveBusinessId.get().execute().await()
            preferences.get().registerOnSharedPreferenceChangeListener(this@StoreImpl)
            updateLastSyncEverythingTime(businessId)
        }
            .subscribeOn(database())
            .observeOn(worker())
            .subscribe()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key?.contains(PREF_BUSINESS_LAST_SYNC_EVERYTHING_TIME) == true) {
            rxCompletable {
                val businessId = getActiveBusinessId.get().execute().await()
                updateLastSyncEverythingTime(businessId)
            }
                .subscribeOn(worker())
                .observeOn(worker())
                .subscribe()
        }
    }

    /********************************* Suppliers *********************************/
    override fun listSuppliers(businessId: String): Observable<List<Supplier>> {
        return supplierDao.get().listSuppliers(businessId)
            .doOnComplete { Timber.i("SupplierThread listSuppliers = ${Thread.currentThread()}") }
            .subscribeOn(database())
            .observeOn(worker())
            .map {
                CommonUtils.mapList(it, DbEntityMapper.SUPPLIER_WITH_TXN_INFO(businessId).reverse())
            }
            .toObservable()
    }

    override fun getSuppliersCount(businessId: String): Observable<Long> {
        return supplierDao.get().getSuppliersCount(businessId)
            .subscribeOn(database())
            .observeOn(worker())
    }

    override fun getActiveSuppliersCount(businessId: String): Flow<Long> {
        return supplierDao.get().getActiveSuppliersCount(businessId)
    }

    override fun listActiveSuppliers(businessId: String): Observable<List<Supplier>> {
        return supplierDao.get().listActiveSuppliers(businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .map {
                CommonUtils.mapList(it, DbEntityMapper.SUPPLIER(businessId).reverse())
            }
            .toObservable()
    }

    override fun listActiveSuppliersByFlyweight(businessId: String): Flow<List<FlyweightSupplier>> {
        return supplierDao.get().listActiveSuppliersByFlyweight(businessId)
    }

    override fun listActiveSuppliersIds(businessId: String): Observable<List<String>> {
        return supplierDao.get().listActiveSuppliersIds(businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .toObservable()
    }

    override fun getSupplier(supplierId: String, businessId: String): Observable<Supplier> {
        return supplierDao.get().getSupplier(supplierId)
            .subscribeOn(database())
            .map<Supplier> { supplier -> DbEntityMapper.SUPPLIER_WITH_TXN_INFO(businessId).reverse().convert(supplier) }
            .toObservable()
    }

    override fun getSuppliers(businessId: String): Observable<List<Supplier>> {
        return supplierDao.get().getSuppliers(businessId)
            .subscribeOn(database())
            .map { supplier ->
                CommonUtils.mapList(supplier, DbEntityMapper.SUPPLIER_WITH_TXN_INFO(businessId).reverse())
            }
            .toObservable()
    }

    override fun getSupplierByMobile(mobile: String, businessId: String): Single<Supplier> {
        return supplierDao.get().getSupplierByMobile(mobile, businessId)
            .subscribeOn(database())
            .map<Supplier> { supplier -> DbEntityMapper.SUPPLIER_WITH_TXN_INFO(businessId).reverse().convert(supplier) }
    }

    override suspend fun getIsBlocked(businessId: String, supplierId: String): Boolean {
        return supplierDao.get().getState(businessId, supplierId) == Supplier.BLOCKED
    }

    override suspend fun getIsAddTransactionRestricted(businessId: String, supplierId: String): Boolean {
        return supplierDao.get().getIsAddTransactionRestricted(businessId, supplierId)
    }

    override fun saveSupplier(supplier: Supplier, businessId: String): Completable {
        return Completable
            .fromAction {
                var supplier_ = supplier
                if (supplierDao.get().supplierExists(supplier.id) > 0) {
                    // preserving the lastViewTime , before replacing every supplier to db
                    val oldValue = supplierDao.get().getSupplier(supplierId = supplier.id).blockingFirst()
                    if (oldValue.lastViewTime == null) { // if no lastViewTime , the we consider current time as lastViewTime
                        oldValue.lastViewTime = currentDateTime()
                            .minusSeconds(10) // .minusSeconds(10) is a hack to show unread count even for very first transaction
                    }
                    supplier_ = supplier.copy(lastViewTime = oldValue.lastViewTime)
                } else {
                    supplier_.copy(
                        lastViewTime = currentDateTime().minusSeconds(10)
                    ) // .minusSeconds(10) is a hack to show unread count even for very first transaction
                }
                supplierDao.get().saveSupplier(DbEntityMapper.SUPPLIER(businessId).convert(supplier_)!!)
            }
            .subscribeOn(database())
            .observeOn(worker())
    }

    // Updating lastView time of a supplier(returning from server) by comparing current local copy of supplier
    private fun updateLastViewTimeFromCurrentSuppliers(
        lastViewedSuppliers: List<`in`.okcredit.merchant.suppliercredit.store.database.Supplier>,
        supplier: Supplier,
    ): Supplier {
        var returnSupplier = supplier.copy(
            lastViewTime = currentDateTime().minusSeconds(10)
        ) // .minusSeconds(10) is a hack to show unread count even for very first transaction
        lastViewedSuppliers.forEach {
            if (it.id == returnSupplier.id) {
                returnSupplier = returnSupplier.copy(lastViewTime = it.lastViewTime)
            }
        }
        return returnSupplier
    }

    override fun saveSuppliers(suppliers: List<Supplier>, businessId: String): Completable {
        return supplierDao.get().listActiveSuppliers(businessId)
            .firstOrError()
            .flatMapCompletable {
                val lastViewedSuppliers = it.filter { it.lastViewTime != null }

                val suppliersMapped = suppliers.map {
                    updateLastViewTimeFromCurrentSuppliers(lastViewedSuppliers, it)
                }

                val list = Utils.mapList(suppliersMapped, DbEntityMapper.SUPPLIER(businessId))
                    .toTypedArray<`in`.okcredit.merchant.suppliercredit.store.database.Supplier>()
                return@flatMapCompletable Completable.fromAction {
                    supplierDao.get().resetSupplierList(businessId, *list)
                }
            }
            .subscribeOn(database())
            .observeOn(worker())
    }

    /********************************* Transactions *********************************/
    override fun listTransactions(supplierId: String, businessId: String): Observable<List<Transaction>> {
        return supplierDao.get().getSupplier(supplierId)
            .subscribeOn(database())
            .flatMap {
                supplierDao.get().listTransactions(supplierId, it.txnStartTime, businessId)
            }
            .map { transactionList ->
                setupImageUrlsInTransactionList(businessId, transactionList)
            }
            .doOnError {
                Timber.i(it)
            }
            .toObservable()
    }

    override fun listTransactionsSortedByBillDate(
        supplierId: String,
        businessId: String,
    ): Observable<List<Transaction>> {
        return supplierDao.get().getSupplier(supplierId)
            .subscribeOn(database())
            .flatMap { supplierDao.get().listTransactionsSortedByBillDate(supplierId, it.txnStartTime, businessId) }
            .map { transactionList -> setupImageUrlsInTransactionList(businessId, transactionList) }
            .toObservable()
    }

    private fun setupImageUrlsInTransactionList(businessId: String, transactionList: List<DbTransaction>) =
        transactionList.mapNotNull { transaction ->
            DbEntityMapper.TRANSACTION(businessId).reverse().convert(transaction)
                ?.apply { finalReceiptUrl = imageCache.get().getImage(receiptUrl) }
        }

    override fun listTransactions(
        supplierId: String,
        txnStartTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return supplierDao.get().getSupplier(supplierId)
            .subscribeOn(database())
            .flatMap {
                supplierDao.get().listTransactions(supplierId, txnStartTime, businessId)
            }
            .map { txns ->
                CommonUtils.mapList(txns, DbEntityMapper.TRANSACTION(businessId).reverse())
            }
            .doOnError {
                Timber.i(it)
            }
            .toObservable()
    }

    override fun listDirtyTransactions(businessId: String): Observable<List<Transaction>> {
        return supplierDao.get().listDirtyTransactions(businessId)
            .subscribeOn(database())
            .map { txns ->
                CommonUtils.mapList(txns, DbEntityMapper.TRANSACTION(businessId).reverse())
            }
            .toObservable()
    }

    override fun getTransaction(txnId: String, businessId: String): Observable<Transaction> {
        return supplierDao.get().geTransaction(txnId)
            .subscribeOn(database())
            // .observeOn(ThreadUtils.worker())
            .map<Transaction> { txn ->
                DbEntityMapper.TRANSACTION(businessId).reverse().convert(txn)
            }
            .map {
                it.apply {
                    finalReceiptUrl = imageCache.get().getImage(receiptUrl)
                }
            }
            .toObservable()
    }

    override fun saveTransaction(txn: Transaction, businessId: String): Completable {
        return Completable
            .fromAction {
                supplierDao.get().saveTransaction(DbEntityMapper.TRANSACTION(businessId).convert(txn)!!)
            }
            .subscribeOn(database())
    }

    override fun saveTransactions(transactions: List<Transaction>, businessId: String): Completable {
        return Completable
            .fromAction {
                val list = Utils.mapList(transactions, DbEntityMapper.TRANSACTION(businessId))
                    .toTypedArray<`in`.okcredit.merchant.suppliercredit.store.database.Transaction>()

                supplierDao.get().saveTransaction(*list)
            }
            .subscribeOn(database())
    }

    override fun removeAllTransaction(supplierId: String): Completable {

        return Completable
            .fromAction {
                supplierDao.get().removeAllTransaction(supplierId)
            }
            .subscribeOn(database())
    }

    /********************************* Date Time *********************************/
    override fun setLastViewTime(supplierId: String, time: Long): Completable {
        return Completable
            .fromAction { supplierDao.get().updateLastViewTime(supplierId, time) }
            .subscribeOn(database())
    }

    override fun setLastActivityTime(supplierId: String, time: Long): Completable {
        return Completable
            .fromAction { supplierDao.get().updateLastActivityTime(supplierId, time) }
            .subscribeOn(database())
    }

    override fun getLastSyncEverythingTime(): Observable<Pair<Boolean, DateTime?>> {
        return lastSyncEverythingTime.hide().distinctUntilChanged()
    }

    override fun setLastSyncEverythingTime(time: DateTime?, businessId: String): Completable {
        return rxCompletable {
            if (time != null) {
                preferences.get().set(PREF_BUSINESS_LAST_SYNC_EVERYTHING_TIME, time.millis, Scope.Business(businessId))
            }
        }.subscribeOn(database())
    }

    override fun getSupplierBalance(businessId: String): Observable<Long> {
        return supplierDao.get().getSupplierBalance(businessId)
    }

    override fun clearLastSyncEverythingTime(): Completable {
        return clearLastSyncEverythingTimeForAllBusinesses.get().execute()
            .subscribeOn(database())
    }

    override fun removeTransaction(txnId: String): Completable {
        return Completable
            .fromAction {
                supplierDao.get().removeTransaction(txnId)
            }
            .subscribeOn(database())
    }

    private fun updateLastSyncEverythingTime(businessId: String) {
        val time =
            preferences.get().blockingGetLong(PREF_BUSINESS_LAST_SYNC_EVERYTHING_TIME, Scope.Business(businessId))
        if (time == 0L) {
            lastSyncEverythingTime.onNext(false to null)
        } else {
            lastSyncEverythingTime.onNext(true to DateTime(time))
        }
    }

    override fun deleteSupplierTable(): Completable {
        return Completable.fromAction {
            supplierDao.get().deleteAllSuppliers()
        }
            .subscribeOn(database())
    }

    override fun deleteTransactionTable(): Completable {
        return Completable.fromAction {
            supplierDao.get().deleteAllTransactions()
        }
            .subscribeOn(database())
    }

    override fun cancelWorker(): Completable {
        return Completable.fromAction {
            workManager.get().cancelAllWorkByTag(SyncerImpl.WORKER_TAG_SUPPLIER)
            workManager.get().cancelAllWorkByTag(SyncerImpl.WORKER_TAG_TRANSACTION)
            workManager.get().cancelAllWorkByTag(SyncerImpl.WORKER_TAG_SYNC_EVERYTHING)
        }
            .subscribeOn(database())
    }

    override fun clear(): Completable {
        return rxCompletable {
            preferences.get().clear()
        }
    }

    override fun saveSupplierEnabledCustomerIds(customerIds: List<String>, businessId: String): Completable {
        return keyValService.get()
            .put(PREF_BUSINESS_SC_ENABLED_CUSTOMERS, customerIds.toString(), Scope.Business(businessId))
    }

    override fun updateSupplierName(updatedName: String, supplierId: String): Completable {
        return Completable
            .fromAction { supplierDao.get().updateSupplierName(updatedName, supplierId) }
            .subscribeOn(database())
    }

    override fun listSupplierTransactionsBetweenBillDate(
        supplierId: String,
        customerTxnStartTimeInMilliSec: Long,
        startTimeInMilliSec: Long,
        endTimeInMilliSec: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return supplierDao.get().listCustomerActiveTransactionsBetweenBillDate(
            supplierId, customerTxnStartTimeInMilliSec, startTimeInMilliSec,
            endTimeInMilliSec,
            businessId
        )
            .subscribeOn(database())
            .observeOn(worker())
            .map { CommonUtils.mapList(it, DbEntityMapper.TRANSACTION(businessId).reverse()) }
    }

    override fun saveRestrictTxnEnabledCustomers(customerIds: List<String>, businessId: String): Completable {
        return keyValService.get()
            .put(PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS, customerIds.toString(), Scope.Business(businessId))
    }

    override fun saveAccountIdsListWithAddTransactionRestricted(
        accountIdsListWithAddTransactionRestricted: List<String>,
        businessId: String,
    ): Completable {
        return keyValService.get().put(
            PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS,
            accountIdsListWithAddTransactionRestricted.toString(),
            Scope.Business(businessId)
        )
    }

    override fun listActiveTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Flowable<List<`in`.okcredit.merchant.suppliercredit.store.database.Transaction?>?>? {
        return supplierDao.get().listActiveTransactionsBetweenBillDate(startTime, endTime, businessId)
    }

    override fun getSortType(businessId: String): Observable<HomeSortType> =
        preferences.get().getInt(PREF_BUSINESS_SORT_TYPE, Scope.Business(businessId), HomeSortType.ACTIVITY.value)
            .asObservable()
            .map { HomeSortType.fromValue(it) }

    override fun setSortType(type: HomeSortType, businessId: String) = rxCompletable {
        preferences.get().set(PREF_BUSINESS_SORT_TYPE, type.value, Scope.Business(businessId))
    }

    override fun saveNotificationReminder(notificationReminder: List<NotificationReminder>): Completable {
        return supplierDao.get().insertNotificationReminder(notificationReminder)
            .subscribeOn(database())
    }

    override fun getStartTimeNotificationReminder(businessId: String): Single<String> {
        return supplierDao.get().getNotificationReminderStartTime(businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .onErrorResumeNext {
                if (it is EmptyResultSetException) return@onErrorResumeNext Single.just("0")
                else throw it
            }
    }

    override fun getNotificationReminder(businessId: String): Single<List<NotificationReminderData>> {
        return supplierDao.get().getNotificationReminders(DateTimeUtils.currentDateTime().millis.toSeconds(), businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .onErrorResumeNext {
                if (it is EmptyResultSetException)
                    return@onErrorResumeNext Single.just(listOf<NotificationReminderData>())
                else throw it
            }
    }

    override fun getProcessedNotificationReminder(businessId: String): Single<List<NotificationReminder>> {
        return supplierDao.get()
            .getNotificationRemindersWithAction(ApiEntityMapper.NotificationReminderStatus.NO_ACTION.status, businessId)
            .subscribeOn(database())
            .observeOn(worker())
            .onErrorResumeNext {
                if (it is EmptyResultSetException)
                    return@onErrorResumeNext Single.just(listOf<NotificationReminder>())
                else throw it
            }
    }

    override fun deleteProcessedNotificationReminder(notificationReminder: List<NotificationReminder>): Completable {
        return supplierDao.get().deletedSyncedReminders(notificationReminder)
            .subscribeOn(database())
            .observeOn(worker())
    }

    override fun updateNotificationReminderStatusById(notificationId: String, status: Int): Completable {
        return supplierDao.get().updateNotificationReminder(notificationId, status)
            .subscribeOn(database())
            .observeOn(worker())
    }

    override suspend fun getLatestTransactionCreateTimeOnSupplier(supplierId: String, businessId: String): DateTime {
        return supplierDao.get().getLatestTransactionCreateTime(supplierId, businessId)
    }
}
