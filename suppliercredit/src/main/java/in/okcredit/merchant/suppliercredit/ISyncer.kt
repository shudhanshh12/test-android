package `in`.okcredit.merchant.suppliercredit

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime

interface ISyncer {
    fun getLastSyncEverythingTime(): Observable<Pair<Boolean, DateTime?>>

    fun setLastSyncEverythingTime(time: DateTime, businessId: String): Completable

    fun syncSupplier(supplierId: String, businessId: String): Completable // TODO: Change Name

    fun syncSpecificSupplier(supplierId: String, businessId: String): Completable // TODO: Change Name //

    fun scheduleSyncSupplier(supplierId: String, businessId: String): Completable

    fun scheduleSyncTransaction(txnId: String, businessId: String): Completable

    fun syncEverything(businessId: String): Completable

    fun syncAllDirtyTransactions(businessId: String): Completable

    fun syncAllTransactions(startTime: DateTime? = null, businessId: String): Completable

    fun syncAllSuppliers(businessId: String): Completable

    fun syncDirtyTransaction(transactionId: String, businessId: String): Single<String>

    fun syncSupplierEnabledCustomerIds(businessId: String): Completable

    fun scheduleSyncSupplierEnabledCustomerIds(businessId: String): Completable

    fun executeSyncSupplierAndTransactions(supplierId: String, businessId: String): Completable

    fun scheduleNotificationReminderSync(businessId: String): Completable

    fun syncNotificationReminder(businessId: String): Completable

    fun syncUpdateNotificationReminder(businessId: String): Completable
}
