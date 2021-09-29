package `in`.okcredit.merchant.core

import `in`.okcredit.accounting_core.contract.QuickAddCustomerModel
import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.merchant.core.common.CoreUtils
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.common.TimestampUtils
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import `in`.okcredit.merchant.core.model.TransactionImage
import `in`.okcredit.merchant.core.model.bulk_reminder.CoreLastReminderSendTime
import `in`.okcredit.merchant.core.model.bulk_reminder.LastReminderSendTime
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionResponse
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import `in`.okcredit.merchant.core.store.database.CoreDbReminderProfile
import com.squareup.moshi.JsonClass
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.Flow
import tech.okcredit.base.network.RequiresNetwork

sealed class Command(
    @Transient var id: String = CoreUtils.generateRandomId(),
    @Transient var timestamp: Timestamp = TimestampUtils.currentTimestamp(),
    @Transient var commandType: CommandType,
    var transactionId: String = "",
    var transactionImages: List<TransactionImage>? = null,
    var customerId: String = "",
) {

    //  Refer CommandTypeMapper for reverse mappings
    enum class CommandType(val code: Int) {
        UNKNOWN(0),

        CREATE_TRANSACTION(1),
        UPDATE_TRANSACTION_NOTE(2),
        DELETE_TRANSACTION(3),
        UPDATE_TRANSACTION_IMAGES(4),
        CREATE_TRANSACTION_IMAGE(5),
        DELETE_TRANSACTION_IMAGE(6),
        UPDATE_TRANSACTION_AMOUNT(7),

        CREATE_CUSTOMER_DIRTY(8),
        CREATE_CUSTOMER_IMMUTABLE(9)
    }

    @JsonClass(generateAdapter = true)
    class CreateTransaction(
        customerId: String,
        transactionId: String,
        val type: Transaction.Type,
        val amount: Long,
        val imagesUriList: List<String> = listOf(),
        val note: String? = null,
        val billDate: Timestamp? = null,
        val inputType: String? = null,
        val voiceId: String? = null,
    ) : Command(
        commandType = CommandType.CREATE_TRANSACTION,
        transactionId = transactionId,
        customerId = customerId
    )

    @JsonClass(generateAdapter = true)
    class UpdateTransactionNote(
        transactionId: String,
        val note: String?,
    ) : Command(commandType = CommandType.UPDATE_TRANSACTION_NOTE, transactionId = transactionId)

    @JsonClass(generateAdapter = true)
    class DeleteTransaction(
        transactionId: String,
    ) : Command(commandType = CommandType.DELETE_TRANSACTION, transactionId = transactionId)

    @JsonClass(generateAdapter = true)
    class UpdateTransactionImages(
        val updatedImagesUriList: List<String>,
        transactionId: String,
    ) : Command(commandType = CommandType.UPDATE_TRANSACTION_IMAGES, transactionId = transactionId)

    @JsonClass(generateAdapter = true)
    class UpdateTransactionAmount(
        transactionId: String,
        val amount: Long,
    ) : Command(commandType = CommandType.UPDATE_TRANSACTION_AMOUNT, transactionId = transactionId)

    @JsonClass(generateAdapter = true)
    class CreateTransactionImage(
        transactionId: String,
        val imageId: String,
        val url: String,
    ) : Command(commandType = CommandType.CREATE_TRANSACTION_IMAGE, transactionId = transactionId)

    @JsonClass(generateAdapter = true)
    class DeleteTransactionImage(
        transactionId: String,
        val imageId: String,
    ) : Command(commandType = CommandType.DELETE_TRANSACTION_IMAGE, transactionId = transactionId)

    @JsonClass(generateAdapter = true)
    class CreateCustomerDirty(
        customerId: String,
    ) : Command(commandType = CommandType.CREATE_CUSTOMER_DIRTY, customerId = customerId)

    @JsonClass(generateAdapter = true)
    class CreateCustomerImmutable(
        customerId: String,
    ) : Command(commandType = CommandType.CREATE_CUSTOMER_IMMUTABLE, customerId = customerId)
}

interface CoreSdk {
    fun isCoreSdkFeatureEnabled(businessId: String): Single<Boolean>
    fun setCoreSdkFeatureStatus(enable: Boolean, businessId: String): Completable
    fun clearLocalData(businessId: String? = null): Completable
    fun isCoreSdkFeatureEnabledFlow(businessId: String): Flow<Boolean>

    // Transaction
    fun processTransactionCommand(command: Command, businessId: String): Single<Transaction>
    fun listTransactions(businessId: String): Observable<List<Transaction>>
    fun syncTransactionsCommands(businessId: String? = null): Completable
    fun syncTransactions(
        source: String,
        req: BehaviorSubject<SyncState>? = null,
        isFromSyncScreen: Boolean = false,
        isFromForceSync: Boolean = false,
        businessId: String? = null,
    ): Completable

    fun scheduleSyncTransactions(source: String, businessId: String): Completable
    fun isTransactionPresent(transactionId: String): Single<Boolean>
    fun isTransactionForCollectionPresent(collectionId: String, businessId: String): Single<Boolean>
    fun getTransaction(transactionId: String, businessId: String): Observable<Transaction>
    fun getTransactionByCollectionId(collectionId: String, businessId: String): Observable<Transaction>
    fun getAllTransactionsCount(businessId: String): Single<Int>
    fun getTransactionCountByType(type: Int, businessId: String): Single<Int>
    fun listActiveTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>>

    fun listActiveTransactionsBetweenBillDate(
        customerId: String,
        customerTxnTime: Long,
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>>

    fun listTransactionsSortedByBillDate(
        customerId: String,
        startTime: Long,
        businessId: String,
    ): Observable<List<Transaction>>

    fun listTransactions(customerId: String, startTime: Long, businessId: String): Observable<List<Transaction>>
    fun listTransactions(customerId: String, businessId: String): Observable<List<Transaction>>
    fun listNonDeletedTransactionsByBillDate(customerId: String, businessId: String): Observable<List<Transaction>>
    fun listDirtyTransactions(isDirty: Boolean, businessId: String): Observable<List<Transaction>>
    fun deleteLocalTransactionsForCustomer(accountId: String): Completable
    fun lastUpdatedTransactionTime(businessId: String): Single<Timestamp>
    fun getTransactionsIdsByCreatedTime(startTime: Long, endTime: Long, businessId: String): Single<List<String>>
    fun markTransactionsDirtyAndCreateCommands(transactionIds: List<String>, businessId: String): Completable
    fun bulkSearchTransactions(actionId: String, transactionIds: List<String>, businessId: String): Single<List<String>>
    fun getNumberOfSyncTransactionsTillGivenUpdatedTime(maximumUpdateTime: Long, businessId: String): Single<Int>

    @RequiresNetwork
    fun getTxnAmountHistory(transactionId: String, businessId: String): Single<TransactionAmountHistory>

    // Customer
    fun createCustomer(
        description: String,
        mobile: String,
        profileImage: String? = null,
        businessId: String,
    ): Single<Customer>

    fun deleteCustomer(customerId: String, businessId: String): Completable
    fun updateCustomer(
        customerId: String,
        desc: String,
        address: String?,
        profileImage: String?,
        mobile: String?,
        lang: String?,
        reminderMode: String?,
        txnAlertEnabled: Boolean,
        isForTxnEnable: Boolean,
        dueInfoActiveDate: Timestamp?,
        updateDueCustomDate: Boolean,
        deleteDueCustomDate: Boolean,
        addTransactionPermission: Boolean,
        updateAddTransactionRestricted: Boolean,
        blockTransaction: Int,
        updateBlockTransaction: Boolean,
        businessId: String,
    ): Completable

    fun syncCustomer(customerId: String, businessId: String? = null): Completable
    fun scheduleSyncCustomer(customerId: String, businessId: String): Completable
    fun syncCustomers(businessId: String? = null): Completable
    fun scheduleSyncCustomers(businessId: String): Completable
    fun reactivateCustomer(
        name: String?,
        customerId: String,
        localProfileImage: String?,
        businessId: String,
    ): Single<Customer>

    fun markActivityAsSeen(customerId: String): Completable
    fun getCustomerCount(businessId: String): Observable<Int>
    fun getActiveCustomerCount(businessId: String): Observable<Long>
    fun getCustomer(customerId: String): Observable<Customer>
    fun getCustomerByMobile(mobile: String, businessId: String): Single<Customer>
    fun listCustomers(businessId: String): Observable<List<Customer>>
    fun listCustomersByLastPayment(businessId: String): Observable<List<Customer>>
    fun listActiveCustomers(businessId: String): Observable<List<Customer>>

    fun listActiveCustomersIds(businessId: String): Observable<List<String>>
    fun deleteLocalCustomer(accountId: String): Completable
    fun updateLocalCustomerDescription(description: String, customerId: String): Completable

    fun getFirstTransaction(businessId: String): Single<Transaction>

    fun getLastTransaction(businessId: String): Single<Transaction>

    fun getLatestTransactionAddedByCustomer(customerId: String, businessId: String): Single<Transaction>

    fun getLatestTransaction(customerId: String): Single<Transaction>

    fun quickAddTransaction(
        customer: QuickAddCustomerModel,
        amount: Long,
        type: Transaction.Type,
        profileImageUploadUrl: String? = null,
        businessId: String,
    ): Single<QuickAddTransactionResponse>

    fun putCustomer(customer: Customer, businessId: String): Completable

    fun putTransaction(transaction: Transaction, businessId: String): Completable

    fun coreUpdateCustomerAddTransactionPermission(accountID: String, isDenied: Boolean): Completable

    fun getDefaulters(businessId: String): Observable<List<Customer>>

    fun getTransactionIdForCollection(collectionId: String, businessId: String): Single<String>

    fun getLiveSalesCustomerId(businessId: String): Single<String>

    suspend fun getCustomersByMobile(mobile: String, businessId: String): List<Customer>

    suspend fun deleteImmutableAccount(customerId: String)

    fun getUnSyncedCustomerCount(businessId: String): Flow<Int>

    fun getImmutableCustomersCount(businessId: String): Flow<Int>

    fun getCustomersWithBalanceDue(businessId: String): Observable<List<Customer>>

    fun listOnlineTransactions(customerId: String): Observable<List<Transaction>>

    // bulk Reminder v2
    fun getDefaultersDataForBanner(defaulterSince: String, businessId: String): Flow<BulkReminderDbInfo>

    fun getDefaultersForPendingReminders(defaulterSince: String, businessId: String): Flow<List<CoreDbReminderProfile>>
    fun getDefaultersForTodaysReminders(defaulterSince: String, businessId: String): Flow<List<CoreDbReminderProfile>>

    suspend fun updateLastReminderSendTime(customerId: String, lastReminderSentTime: Timestamp, businessId: String)
    suspend fun getDirtyLastReminderSendTime(
        customerIds: List<String>,
        businessId: String,
    ): List<CoreLastReminderSendTime>

    suspend fun setCustomersLastReminderSendTimeToServer(
        lastReminderSendTimeList: List<LastReminderSendTime>,
        businessId: String,
    )

    suspend fun getIsBlocked(businessId: String, customerId: String): Boolean

    suspend fun getIsAddTransactionRestricted(businessId: String, customerId: String): Boolean
}
