package `in`.okcredit.merchant.core.store

import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionImage
import `in`.okcredit.merchant.core.model.bulk_reminder.CoreLastReminderSendTime
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import `in`.okcredit.merchant.core.store.database.CoreDbReminderProfile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import `in`.okcredit.merchant.core.store.database.Command as DbCommand
import `in`.okcredit.merchant.core.store.database.Transaction as DbTransaction

interface CoreLocalSource {

    suspend fun createOfflineCustomer(
        description: String,
        mobile: String?,
        profileImage: String?,
        businessId: String,
    ): Customer

    suspend fun listDirtyCustomerCommands(
        pageSize: Int,
        businessId: String,
    ): List<Command>

    suspend fun listImmutableCustomerCommands(
        offset: Int,
        pageSize: Int,
        businessId: String,
    ): List<Command>

    suspend fun getCustomerSus(customerId: String): Customer

    suspend fun putCustomerSus(customer: Customer, businessId: String)

    suspend fun putCustomerSus(customers: List<Customer>, businessId: String)

    suspend fun deleteImmutableAccount(customerId: String)

    suspend fun getTransactionCountForCustomer(customerId: String): Int

    suspend fun replaceCustomerId(oldId: String, newId: String)

    suspend fun updateCustomerSyncStatus(customerId: String, customerSyncStatus: Customer.CustomerSyncStatus)

    suspend fun getCommandForCustomerId(customerId: String, businessId: String): Command?

    suspend fun updateCustomerCommandType(commandId: String, type: Command.CommandType)

    suspend fun getCustomersByMobile(mobile: String, businessId: String): List<Customer>

    fun getUnSyncedCustomersCount(businessId: String): Flow<Int>

    fun getCommandsCount(type: List<Command.CommandType>, businessId: String): Flow<Int>

    suspend fun getIsBlocked(businessId: String, customerId: String): Boolean

    suspend fun getIsAddTransactionRestricted(businessId: String, customerId: String): Boolean

    // ---------------------------------------------

    fun createTransaction(
        transaction: Transaction,
        command: Command.CreateTransaction,
        businessId: String,
    ): Completable

    fun listTransactions(businessId: String): Observable<List<Transaction>>

    fun listTransactionsCommandsForCleanCustomers(count: Int, businessId: String): Observable<List<Command>>

    fun listTransactionsCommandsForCleanCustomers(businessId: String): Observable<List<Command>>

    fun updateTransactionNote(command: Command.UpdateTransactionNote, businessId: String): Completable

    fun updateTransactionAmount(command: Command.UpdateTransactionAmount, businessId: String): Completable

    fun isTransactionPresent(id: String): Single<Boolean>

    fun putTransaction(transaction: Transaction, businessId: String): Completable

    fun putTransactions(transactionList: List<Transaction>, businessId: String): Completable

    fun lastUpdatedTransactionTime(businessId: String): Single<Timestamp>

    fun clearTransactionTableForBusiness(businessId: String): Completable

    fun clearTransactionTable(): Completable

    fun deleteCommands(ids: List<String>): Completable

    fun deleteTransaction(command: Command.DeleteTransaction, businessId: String): Completable

    fun clearCommandTableForBusiness(businessId: String): Completable

    fun clearCommandTable(): Completable

    fun markTransactionDirty(ids: List<String>, isDirty: Boolean): Completable

    fun replaceTransactionId(oldId: String, newId: String): Completable

    fun getDbTransactionsWithImageId(imageId: String, businessId: String): Single<List<DbTransaction>>

    fun getDbCommandsWithImageId(imageId: String, businessId: String): Single<List<DbCommand>>

    fun updateTransactionImagesAndCommandValues(
        transactionIdToImagesList: List<Pair<String, String>>,
        commandIdToValueList: List<Pair<Int, String>>,
    ): Completable

    fun getTransaction(transactionId: String): Observable<Transaction>

    fun updateTransactionImagesAndInsertCommands(
        images: List<TransactionImage>,
        transactionId: String,
        commands: List<Command>,
        businessId: String,
    ): Completable

    fun getTransactionByCollectionId(collectionId: String, businessId: String): Observable<Transaction>

    fun getAllTransactionsCount(businessId: String): Single<Int>

    fun getTransactionsCountTillGivenUpdatedTime(time: Timestamp, businessId: String): Single<Int>

    fun isTransactionForCollectionPresent(collectionId: String, businessId: String): Single<Boolean>

    fun listActiveTransactionsBetweenBillDate(
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>>

    fun listActiveTransactionsBetweenBillDate(
        customerId: String,
        customerTxnTime: Timestamp,
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>>

    fun listTransactionsSortedByBillDate(
        customerId: String,
        startTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>>

    fun listTransactions(customerId: String, startTime: Timestamp, businessId: String): Observable<List<Transaction>>

    fun listTransactions(customerId: String, businessId: String): Observable<List<Transaction>>

    fun listNonDeletedTransactionsByBillDate(customerId: String, businessId: String): Observable<List<Transaction>>

    fun listDirtyTransactions(isDirty: Boolean, businessId: String): Observable<List<Transaction>>

    fun getCustomer(customerId: String): Observable<Customer>

    fun getCustomerByMobile(mobile: String, businessId: String): Single<Customer>

    fun listCustomers(businessId: String): Observable<List<Customer>>

    fun listCustomersByLastPayment(businessId: String): Observable<List<Customer>>

    fun listActiveCustomers(businessId: String): Observable<List<Customer>>

    fun listActiveCustomersIds(businessId: String): Observable<List<String>>

    fun getCustomerCount(businessId: String): Observable<Int>

    fun getActiveCustomerCount(businessId: String): Observable<Long>

    fun markActivityAsSeen(customerId: String): Completable

    fun putCustomer(customer: Customer, businessId: String): Completable

    fun resetCustomerList(customers: List<Customer>, businessId: String): Completable

    fun clearCustomerTableForBusiness(businessId: String): Completable

    fun clearCustomerTable(): Completable

    fun deleteCustomer(accountId: String): Completable

    fun deleteLocalTransactionsForCustomer(accountId: String): Completable

    fun updateCustomerDescription(description: String, customerId: String): Completable

    fun getFirstTransaction(businessId: String): Single<Transaction>

    fun getLastTransaction(businessId: String): Single<Transaction>

    fun getTransactionsIdsByCreatedTime(
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Single<List<String>>

    fun markTransactionDirtyAndInsertCommandIfNotPresent(
        transactionId: String,
        command: Command.CreateTransaction,
        businessId: String,
    ): Completable

    fun getLatestTransactionAddedByCustomer(customerId: String, businessId: String): Single<Transaction>

    fun getLatestTransaction(customerId: String): Single<Transaction>

    fun updateCustomerAddTransactionPermission(accountID: String, isDenied: Boolean): Completable

    fun getSuggestedCustomerIdsForAddTransaction(businessId: String): Single<List<String>>

    fun replaceSuggestedCustomerIdsForAddTransaction(ids: List<String>, businessId: String): Completable

    fun getTransactionCountByType(type: Int, businessId: String): Single<Int>

    fun getDefaulters(businessId: String): Observable<List<Customer>>

    fun getTransactionIdForCollection(collectionId: String, businessId: String): Single<String>

    fun getLiveSalesCustomerId(businessId: String): Single<String>

    fun getCustomersWithBalanceDue(businessId: String): Observable<List<Customer>>

    fun listOnlineTransactions(customerId: String): Observable<List<Transaction>>

    // Bulk Reminder V2
    fun getDefaultersDataForBanner(defaulterSince: String, businessId: String): Flow<BulkReminderDbInfo>

    fun getDefaultersForPendingReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<CoreDbReminderProfile>>

    fun getDefaultersForTodaysReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<CoreDbReminderProfile>>

    suspend fun updateLastReminderSendTime(customerId: String, lastReminderSentTime: Timestamp, businessId: String)

    suspend fun getDirtyLastReminderSendTime(
        customerIds: List<String>,
        businessId: String,
    ): List<CoreLastReminderSendTime>
}
