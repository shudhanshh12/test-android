package `in`.okcredit.merchant.suppliercredit

import `in`.okcredit.merchant.suppliercredit.server.internal.ApiMessages
import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime

interface SupplierRemoteSource {

    fun addSupplier(name: String, mobile: String?, profileImage: String?, businessId: String): Single<Supplier>

    fun updateSuppler(
        supplier: Supplier,
        txnAlertChanged: Boolean,
        state: Int,
        updateState: Boolean,
        businessId: String,
    ): Completable

    fun deleteSupplier(supplierId: String, businessId: String): Completable

    fun getSupplier(supplierId: String, businessId: String): Single<Supplier>

    fun getSuppliers(businessId: String): Single<List<Supplier>>

    fun addTransaction(transaction: Transaction, businessId: String): Single<Transaction>

    fun getTransactionOfSupplier(supplierId: String, businessId: String): Single<List<Transaction>>

    fun getTransactions(startTime: DateTime? = null, businessId: String): Single<List<Transaction>>

    fun getSCEnabledCustomerIds(businessId: String): Single<AccountMetaInfo>

    fun getTransaction(txnId: String, businessId: String): Single<Transaction>

    fun deleteTransaction(txnId: String, businessId: String): Completable

    fun getNotificationReminder(startTime: Long, businessId: String): Single<ApiMessages.NotificationRemindersResponse>

    fun createNetworkReminder(accountId: String, businessId: String): Single<Boolean>

    fun updateNotificationReminder(request: ApiMessages.UpdateNotificationReminder, businessId: String): Single<List<ApiMessages.UpdateNotificationReminderAction>>
}
