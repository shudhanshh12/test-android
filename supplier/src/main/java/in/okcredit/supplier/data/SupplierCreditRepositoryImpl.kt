package `in`.okcredit.supplier.data

import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.merchant.contract.BusinessScopedPreferenceWithActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.*
import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderData
import `in`.okcredit.merchant.suppliercredit.store.database.DbEntityMapper
import `in`.okcredit.merchant.suppliercredit.utils.Utils
import `in`.okcredit.shared.utils.CommonUtils.currentDateTime
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import merchant.okcredit.accounting.contract.HomeSortType
import merchant.okcredit.supplier.contract.IsNetworkReminderEnabled
import merchant.okcredit.suppliercredit.contract.FlyweightSupplier
import org.joda.time.DateTime
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_SC_ENABLED_CUSTOMERS
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SupplierCreditRepositoryImpl @Inject constructor(
    private val store: Lazy<SupplierLocalSource>,
    private val server: Lazy<SupplierRemoteSource>,
    private val syncer: Lazy<ISyncer>,
    private val imageUploader: Lazy<IUploadFile>,
    private val businessScopedPreferenceWithActiveBusinessId: Lazy<BusinessScopedPreferenceWithActiveBusinessId>,
    private val defaultPreferences: Lazy<DefaultPreferences>,
    private val ab: Lazy<AbRepository>,
    private val isNetworkReminderEnabled: Lazy<IsNetworkReminderEnabled>,
) : SupplierCreditRepository {

    override fun removeAllTransaction(supplierId: String): Completable {
        return store.get().removeAllTransaction(supplierId)
    }

    override fun syncSuppliers(businessId: String): Completable {
        return syncer.get().syncAllSuppliers(businessId)
    }

    override fun syncSpecificSupplier(supplierId: String, businessId: String): Completable {
        return syncer.get().syncSpecificSupplier(supplierId, businessId)
    }

    override fun syncEverything(businessId: String): Completable {
        return syncer.get().syncEverything(businessId)
    }

    override fun syncAllTransactions(startTime: DateTime?, businessId: String): Completable {
        return syncer.get().syncAllTransactions(startTime, businessId = businessId)
    }

    override fun syncSupplierEnabledCustomerIds(businessId: String): Completable {
        return syncer.get().syncSupplierEnabledCustomerIds(businessId)
    }

    override fun scheduleSyncSupplierEnabledCustomerIds(businessId: String): Completable {
        return syncer.get().scheduleSyncSupplierEnabledCustomerIds(businessId)
    }

    override fun executeSyncSupplierAndTransactions(supplierId: String, businessId: String): Completable {
        return syncer.get().executeSyncSupplierAndTransactions(supplierId, businessId)
    }

    override fun listSupplierTransactionsBetweenBillDate(
        supplierId: String,
        customerTxnStartTimeInMilliSec: Long,
        startTimeInMilliSec: Long,
        endTimeInMilliSec: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return store.get().listSupplierTransactionsBetweenBillDate(
            supplierId,
            customerTxnStartTimeInMilliSec,
            startTimeInMilliSec,
            endTimeInMilliSec,
            businessId
        )
    }

    override fun updateSupplerName(updatedName: String, supplierId: String): Completable {
        return store.get().updateSupplierName(updatedName, supplierId)
            .observeOn(ThreadUtils.worker())
    }

    companion object {
        const val SUPPLIER_SYNC_INTERVAL_SECONDS = 10 * 60
    }

    override fun listActiveSuppliers(businessId: String): Observable<List<Supplier>> {
        return store.get().listActiveSuppliers(businessId)
            .observeOn(ThreadUtils.worker())
    }

    override fun listActiveSuppliersByFlyweight(businessId: String): Flow<List<FlyweightSupplier>> {
        return store.get().listActiveSuppliersByFlyweight(businessId)
    }

    override fun listActiveSuppliersIds(businessId: String): Observable<List<String>> {
        return store.get().listActiveSuppliersIds(businessId)
            .observeOn(ThreadUtils.worker())
    }

    override fun getSupplierBalance(businessId: String): Observable<Long> {
        return store.get().getSupplierBalance(businessId).observeOn(ThreadUtils.worker())
    }

    override fun getSuppliersCount(businessId: String): Observable<Long> {
        return store.get().getSuppliersCount(businessId)
            .observeOn(ThreadUtils.worker())
    }

    override fun getActiveSuppliersCount(businessId: String): Flow<Long> {
        return store.get().getActiveSuppliersCount(businessId)
    }

    override fun getSuppliers(businessId: String): Observable<List<Supplier>> {
        return store.get().getSuppliers(businessId)
            .flatMap {
                return@flatMap Observable.just(it)
            }
    }

    override fun getSupplier(supplierId: String, businessId: String): Observable<Supplier> {
        return store.get().getSupplier(supplierId, businessId)
            .flatMap {
                val supplier = Observable.just(it)
                if (it.isSyncRequired()) {
                    return@flatMap syncer.get().scheduleSyncSupplier(supplierId, businessId).andThen(supplier)
                }
                return@flatMap supplier
            }
    }

    override fun listTransactions(supplierId: String, businessId: String): Observable<List<Transaction>> {
        return store.get().listTransactions(supplierId, businessId)
    }

    override fun listTransactionsSortedByBillDate(
        supplierId: String,
        businessId: String,
    ): Observable<List<Transaction>> {
        return store.get().listTransactionsSortedByBillDate(supplierId, businessId)
    }

    override fun listTransactions(
        supplierId: String,
        txnStartTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> {
        return store.get().listTransactions(supplierId, txnStartTime, businessId)
    }

    override fun listDirtyTransactions(businessId: String): Observable<List<Transaction>> {
        return store.get().listDirtyTransactions(businessId)
    }

    override fun getTransaction(txnId: String, businessId: String): Observable<Transaction> {
        return store.get().getTransaction(txnId, businessId)
    }

    override fun addSupplier(
        name: String,
        mobile: String?,
        profileImage: String?,
        businessId: String,
    ): Single<Supplier> {
        return server.get()
            .addSupplier(name, mobile, profileImage, businessId)
            .flatMap {
                syncer.get().syncSupplier(it.id, businessId)
                    .doOnComplete { Timber.i("addSupplier completed") }
                    .andThen(Single.just(it))
            }
    }

    // to reactive supplier we call addSupplier supplier once again
    // basically server only changes the state from deleted=true to deleted=false & everything remains same
    override fun reactivateSupplier(supplierId: String, supplierName: String?, businessId: String): Completable {

        return store.get().getSupplier(supplierId, businessId)
            .firstOrError()
            .flatMapCompletable {
                // if user provided new name while reactivation , the  we update with new name
                // or else we take the name that is already available in db
                val name = if (supplierName.isNullOrBlank()) {
                    it.name // name from db
                } else {
                    supplierName // user provided name
                }
                server.get().addSupplier(name, it.mobile, it.profileImage, businessId).ignoreElement()
            }
            .andThen(syncer.get().syncSupplier(supplierId, businessId))
    }

    override fun updateSuppler(
        supplier: Supplier,
        txnAlertChanged: Boolean,
        state: Int,
        updateState: Boolean,
        businessId: String,
    ): Completable {

        val filePath = supplier.profileImage
        val fileUploadSingle: Single<String>

        fileUploadSingle = if (filePath == null || filePath.isEmpty() || updateState) {
            Single.just("")
        } else {
            val receiptUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID().toString() + ".jpg"
            imageUploader.get().schedule(IUploadFile.CUSTOMER_PHOTO, receiptUrl, filePath)
                .observeOn(ThreadUtils.worker())
                .andThen(
                    Single.just(receiptUrl)
                )
        }

        return fileUploadSingle
            .flatMapCompletable {
                val supplier_ = supplier.copy(profileImage = it) // local image uri change to aws image url
                server.get().updateSuppler(
                    supplier_, txnAlertChanged, state, updateState, businessId
                )
            }
            .doOnComplete { Timber.i(">>syncSupplier 1") }
            .andThen(syncer.get().syncSupplier(supplier.id, businessId).doOnComplete { Timber.i(">>syncSupplier 2") })
            .doOnComplete { Timber.i(">>syncSupplier 2") }
    }

    // when supplier is deleted , then we also delete his all transactions from app db and we don't sync this deleted
    // transactions with server , because server handles it in different way when supplier is deleted
    override fun deleteSupplier(supplierId: String, businessId: String): Completable {
        return server.get()
            .deleteSupplier(supplierId, businessId)
            .andThen(
                syncer.get().syncSupplier(supplierId, businessId)
            )
            .andThen(
                store.get().removeAllTransaction(supplierId)
            )
    }

    override fun syncTransaction(
        localTransaction: Transaction,
        businessId: String,
    ): Single<String> {
        return syncer.get().syncDirtyTransaction(localTransaction.id, businessId)
    }

    override fun addTransaction(
        transaction: Transaction,
        businessId: String,
    ): Completable {
        Timber.i("addTransaction 1")

        val filePath = transaction.receiptUrl
        val fileUploadSingle: Single<String>

        fileUploadSingle = if (filePath == null || filePath.isEmpty()) {
            Single.just("")
        } else {
            val receiptUrl = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + UUID.randomUUID().toString() + ".jpg"
            imageUploader.get().schedule(IUploadFile.CUSTOMER_PHOTO, receiptUrl, filePath)
                .observeOn(ThreadUtils.worker())
                .andThen(
                    Single.just(receiptUrl)
                )
        }

        return fileUploadSingle
            .flatMapCompletable {
                val txn = transaction.copy(receiptUrl = it) // local image uri changed to aws image url
                store.get().saveTransaction(txn, businessId)
            }
            .andThen(syncer.get().scheduleSyncTransaction(txnId = transaction.id, businessId))
            .doOnComplete { Timber.i(">> addTransaction 2") }
    }

    override fun deleteTransaction(txnId: String, businessId: String): Completable {
        return store.get()
            .getTransaction(txnId, businessId)
            .firstOrError()
            .doOnEvent { t1, t2 -> Timber.i("deleteTransaction") }
            .observeOn(ThreadUtils.worker())
            .map {
                val now = currentDateTime()
                return@map it.copy(
                    deleted = true,
                    deleteTime = now,
                    updateTime = now,
                    syncing = false
                )
            }

            .flatMapCompletable {
                store.get().saveTransaction(it, businessId)
                    .observeOn(ThreadUtils.worker())
            }
            .andThen(syncer.get().scheduleSyncTransaction(txnId = txnId, businessId))
    }

    override fun getSupplierByMobile(mobile: String, businessId: String): Single<Supplier> {
        return store.get()
            .getSupplierByMobile(mobile, businessId)
            .observeOn(ThreadUtils.worker())
            .doOnError {
                Timber.i(it.toString())
            }
    }

    override suspend fun getIsBlocked(businessId: String, supplierId: String): Boolean {
        return store.get().getIsBlocked(businessId, supplierId)
    }

    override suspend fun getIsAddTransactionRestricted(businessId: String, supplierId: String): Boolean {
        return store.get().getIsAddTransactionRestricted(businessId, supplierId)
    }

    override fun markActivityAsSeen(supplierId: String): Completable {
        return store.get()
            .setLastViewTime(supplierId, currentDateTime().millis / 1000L)
            .observeOn(ThreadUtils.worker())
    }

    override fun setLastActivityTime(supplierId: String): Completable {
        return store.get()
            .setLastActivityTime(supplierId, currentDateTime().millis / 1000L)
            .observeOn(ThreadUtils.worker())
    }

    override fun clearLocalData(): Completable {
        return store.get().deleteSupplierTable()
            .doOnComplete { Timber.i("deleteSupplierTable 1") }
            .andThen(store.get().deleteTransactionTable())
            .doOnComplete { Timber.i("deleteSupplierTable 2") }
            .andThen(store.get().clearLastSyncEverythingTime())
            .andThen(store.get().cancelWorker())
            .andThen(store.get().clear())
            .andThen(
                businessScopedPreferenceWithActiveBusinessId.get()
                    .delete(defaultPreferences.get(), PREF_BUSINESS_SC_ENABLED_CUSTOMERS)
            )
            .doOnComplete {
                Timber.i("deleteSupplierTable 3")
            }
    }

    override fun listActiveTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>>? {
        return store.get().listActiveTransactionsBetweenBillDate(startTime, endTime, businessId)?.map { transactions ->
            Utils.mapList(transactions, DbEntityMapper.TRANSACTION(businessId).reverse())
        }?.toObservable()
    }

    override fun getSortType(businessId: String) = store.get().getSortType(businessId)

    override fun setSortType(type: HomeSortType, businessId: String) = store.get().setSortType(type, businessId)

    override suspend fun getLatestTransactionCreateTimeOnSupplier(supplierId: String, businessId: String): DateTime {
        return store.get().getLatestTransactionCreateTimeOnSupplier(supplierId, businessId)
    }

    override fun syncNotificationReminder(businessId: String): Completable {
        return isNetworkReminderEnabled.get().execute().flatMapCompletable {
            if (it) {
                return@flatMapCompletable syncer.get().scheduleNotificationReminderSync(businessId)
            } else {
                return@flatMapCompletable Completable.complete()
            }
        }
    }

    override fun getNotificationReminderData(businessId: String): Single<List<NotificationReminderData>> {
        return store.get().getNotificationReminder(businessId)
    }

    override fun createNotificationReminder(accountId: String, businessId: String): Single<Boolean> {
        return server.get().createNetworkReminder(accountId, businessId)
    }

    override fun updateNotificationReminderById(notificationId: String, status: Int): Completable {
        return store.get().updateNotificationReminderStatusById(notificationId, status)
    }
}

internal fun Supplier.isSyncRequired(): Boolean =
    lastSyncTime == null ||
        lastSyncTime!!.isBefore(
            currentDateTime().minusSeconds(SupplierCreditRepositoryImpl.SUPPLIER_SYNC_INTERVAL_SECONDS)
        )
