package `in`.okcredit.merchant.core.store.database

import `in`.okcredit.merchant.core.Command.CommandType
import `in`.okcredit.merchant.core.Command.CommandType.*
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.DIRTY
import `in`.okcredit.merchant.core.model.bulk_reminder.CoreLastReminderSendTime
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

@Suppress("LongLine")
@Dao
interface CoreDatabaseDao {

    @androidx.room.Transaction
    suspend fun createOfflineCustomer(customer: Customer, command: Command) {
        putCustomerSus(customer)
        addCustomerCommand(command)
    }

    @Query("select `id` from Customer where customerSyncStatus = :customerSyncStatus and businessId = :businessId ")
    fun getCustomersIds(customerSyncStatus: Int, businessId: String): Single<List<String>>

    @Query("select `id` from Customer where customerSyncStatus in (:syncStatuses) and businessId = :businessId")
    fun getCustomersIds(syncStatuses: List<Int>, businessId: String): Single<List<String>>

    @androidx.room.Transaction
    suspend fun addCustomerCommand(command: Command) {
        updateCustomerSyncStatus(command.customerId, DIRTY.code)
        insertCommandSus(command) // TODO handle command id conflict
    }

    @Insert
    suspend fun insertCommandSus(command: Command)

    @Query("update `customer` set customerSyncStatus = :customerSyncStatus where id = :customerId")
    suspend fun updateCustomerSyncStatus(customerId: String, customerSyncStatus: Int)

    @Query("select * from Command where customerId = :customerId and businessId = :businessId")
    suspend fun getCommandForCustomerId(customerId: String, businessId: String): Command?

    @Query("update Command set type = :type where commandId = :commandId")
    suspend fun updateCustomerCommandType(commandId: String, type: CommandType)

    @Query("""select * from Command where type = :type and businessId = :businessId order by id asc limit :pageSize""")
    suspend fun listDirtyCustomerCommands(
        pageSize: Int,
        type: CommandType = CREATE_CUSTOMER_DIRTY,
        businessId: String,
    ): List<Command>

    @Query("""select * from Command where businessId = :businessId and type = :type order by id asc limit :pageSize offset :offset""")
    suspend fun listImmutableCustomerCommands(
        offset: Int,
        pageSize: Int,
        type: CommandType = CREATE_CUSTOMER_IMMUTABLE,
        businessId: String,
    ): List<Command>

    @Query("select * from CustomerWithTransactionsInfo where id = :customerId LIMIT 1")
    suspend fun getCustomerSus(customerId: String): CustomerWithTransactionsInfo

    @Query("select * from CustomerWithTransactionsInfo where businessId = :businessId and mobile = :mobile order by customerSyncStatus asc")
    suspend fun getCustomersByMobile(mobile: String, businessId: String): List<CustomerWithTransactionsInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putCustomerSus(customer: Customer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putCustomerSus(customers: Array<Customer>)

    @Query("delete from Customer where id = :accountId")
    suspend fun deleteCustomerSus(accountId: String)

    @androidx.room.Transaction
    suspend fun deleteAccount(accountId: String) {
        deleteLocalTransactions(accountId)
        deleteCustomerSus(accountId)
        deleteCommands(accountId)
    }

    @Query("delete from Command where customerId = :accountId")
    suspend fun deleteCommands(accountId: String)

    @Query("delete from `transaction` where customerId = :accountId")
    suspend fun deleteLocalTransactions(accountId: String)

    @Query("""select count(*) from `Transaction` where customerId = :customerId""")
    suspend fun getTransactionCountForCustomer(customerId: String): Int

    @androidx.room.Transaction
    suspend fun replaceCustomerId(oldId: String, newId: String) {
        replaceCustomerIdForCustomer(oldId, newId)
        replaceCustomerIdForTransaction(oldId, newId)
        replaceCustomerIdsForCommands(oldId, newId)
    }

    @Query("update Customer set id = :newId where id = :oldId")
    suspend fun replaceCustomerIdForCustomer(oldId: String, newId: String)

    @Query("update `Transaction` set customerId = :newId where customerId = :oldId")
    suspend fun replaceCustomerIdForTransaction(oldId: String, newId: String)

    @Query("update Command set customerId = :newId where customerId = :oldId")
    suspend fun replaceCustomerIdsForCommands(oldId: String, newId: String)

    @Query("""select count(*) from Command where businessId = :businessId""")
    fun getUnSyncedCustomersCount(businessId: String): Flow<Int>

    @Query("""select count(*) from Command where type in (:type) and businessId = :businessId """)
    fun getCommandsCount(type: List<CommandType>, businessId: String): Flow<Int>

    @Query("select state from Customer where businessId = :businessId and id = :customerId")
    suspend fun getCustomerState(businessId: String, customerId: String): Int

    @Query("select addTransactionRestricted from Customer where businessId = :businessId and id = :customerId")
    suspend fun getIsAddTransactionRestricted(businessId: String, customerId: String): Boolean

    // ----------------------------

    @androidx.room.Transaction
    fun createTransaction(transaction: Transaction, command: Command) {
        insertTransaction(transaction)
        addTransactionCommand(command)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(vararg transaction: Transaction)

    @androidx.room.Transaction
    fun addTransactionCommand(command: Command) {
        markTransactionDirty(command.transactionId, true)
        insertCommand(command) // TODO handle command id conflict
    }

    @Query("update `transaction` set isDirty = :isDirty where id = :transactionId")
    fun markTransactionDirty(transactionId: String, isDirty: Boolean)

    @Query("update `transaction` set isDirty = :isDirty where id in (:transactionIds)")
    fun markTransactionsDirty(transactionIds: Array<String>, isDirty: Boolean): Completable

    @Insert
    fun insertCommand(command: Command)

    @Query("select * from `transaction` where businessId = :businessId")
    fun listTransactions(businessId: String): Observable<List<Transaction>>

    // NOTE transaction types are added for double protection.
    // Usually customerId filtering would remove CreateCustomer commands and only leave transaction types
    @Query("select * from Command where businessId = :businessId and type in (:transactionTypes) and customerId not in (:offlineCustomers) order by id asc limit :count")
    fun listTransactionsCommandsWithoutOfflineCustomers(
        count: Int,
        offlineCustomers: List<String>,
        transactionTypes: List<CommandType> = listOf(
            CREATE_TRANSACTION,
            UPDATE_TRANSACTION_NOTE,
            DELETE_TRANSACTION,
            UPDATE_TRANSACTION_IMAGES,
            CREATE_TRANSACTION_IMAGE,
            DELETE_TRANSACTION_IMAGE,
            UPDATE_TRANSACTION_AMOUNT,
        ),
        businessId: String,
    ): Observable<List<Command>>

    // NOTE transaction types are added for double protection.
    // Usually customerId filtering would remove CreateCustomer commands and only leave transaction types
    @Query("select * from Command where businessId = :businessId and type in (:transactionTypes) and customerId not in (:offlineCustomers) order by id asc")
    fun listTransactionsCommandsWithoutOfflineCustomers(
        offlineCustomers: List<String>,
        transactionTypes: List<CommandType> = listOf(
            CREATE_TRANSACTION,
            UPDATE_TRANSACTION_NOTE,
            DELETE_TRANSACTION,
            UPDATE_TRANSACTION_IMAGES,
            CREATE_TRANSACTION_IMAGE,
            DELETE_TRANSACTION_IMAGE,
            UPDATE_TRANSACTION_AMOUNT,
        ),
        businessId: String,
    ): Observable<List<Command>>

    @Query("SELECT count(*) FROM Command WHERE transactionId = :transactionId AND type = :commandType")
    fun isCommandTypeForTransactionIdPresent(
        transactionId: String,
        commandType: CommandType,
    ): Int

    @androidx.room.Transaction
    fun updateTransactionNote(
        note: String?,
        command: Command,
    ) {
        updateNote(command.transactionId, note)
        addTransactionCommand(command)
    }

    @Query("update `transaction` set note = :note where id = :transactionId")
    fun updateNote(transactionId: String, note: String?)

    @Query("select count(*) from `transaction` where id = :id")
    fun isTransactionPresent(id: String): Single<Int>

    @Query("select max(updatedAt) from `transaction` where businessId = :businessId")
    fun lastUpdatedTransactionTime(businessId: String): Single<Timestamp>

    @Query("delete from `transaction` where businessId = :businessId")
    fun clearTransactionTable(businessId: String): Completable

    @Query("delete from `transaction`")
    fun deleteTransactionTable(): Completable

    @Query("delete from Command where commandId in (:ids)")
    fun deleteCommands(ids: Array<String>): Completable

    @Query("delete from Command where businessId = :businessId")
    fun clearCommandTable(businessId: String): Completable

    @Query("delete from Command")
    fun deleteCommandTable(): Completable

    @androidx.room.Transaction
    fun deleteTransaction(command: Command) {
        deleteTransaction(true, command.timestamp, command.transactionId)
        addTransactionCommand(command)
    }

    @Query("update `transaction` set isDeleted = :delete, deleteTime = :timestamp where id = :transactionId")
    fun deleteTransaction(delete: Boolean, timestamp: Timestamp, transactionId: String)

    @androidx.room.Transaction
    fun replaceTransactionId(oldId: String, newId: String) {
        replaceTransactionIdForTransaction(oldId, newId)
        replaceTransactionIdsForCommands(oldId, newId)
    }

    @Query("update Command set transactionId = :newId where transactionId = :oldId")
    fun replaceTransactionIdsForCommands(oldId: String, newId: String)

    @Query("update `transaction` set id = :newId where id = :oldId")
    fun replaceTransactionIdForTransaction(oldId: String, newId: String)

    @Query("select * from `transaction` where id = :transactionId limit 1")
    fun getTransaction(transactionId: String): Observable<Transaction>

    @Query("update `transaction` set images = :images where id = :transactionId")
    fun updateTransactionImages(images: String, transactionId: String)

    @androidx.room.Transaction
    fun updateTransactionImagesAndInsertCommands(images: String, transactionId: String, commands: List<Command>) {
        updateTransactionImages(images, transactionId)
        commands.forEach { addTransactionCommand(it) }
    }

    @Query("select * from `transaction` where collectionId = :collectionId and businessId = :businessId")
    fun getTransactionByCollectionId(collectionId: String, businessId: String): Observable<Transaction>

    @Query("select count(*) from `transaction` where collectionId = :collectionId and businessId = :businessId")
    fun isTransactionForCollectionPresent(collectionId: String, businessId: String): Single<Int>

    @Query("select id from `transaction` where collectionId = :collectionId and businessId = :businessId")
    fun getTransactionIdByCollectionId(collectionId: String, businessId: String): Single<String>

    @Query("SELECT * FROM `transaction` WHERE businessId = :businessId AND  billDate > :startTime AND billDate <= :endTime AND isDeleted == 0 ORDER BY billDate DESC")
    fun listActiveTransactionsBetweenBillDate(
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>>

    @Query("SELECT * FROM `transaction` WHERE customerId = :customerId AND createdAt > :customerTxnTime and billDate > :startTime AND billDate <= :endTime AND isDeleted == 0 AND businessId = :businessId ORDER BY billDate DESC")
    fun listActiveTransactionsBetweenBillDate(
        customerId: String,
        customerTxnTime: Timestamp,
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>>

    @Query("select * from `transaction` WHERE customerId = :customerId AND createdAt > :startTime AND businessId = :businessId ORDER BY billDate ASC, createdAt ASC")
    fun listTransactionsSortedByBillDate(
        customerId: String,
        startTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>>

    @Query("select * from `transaction` WHERE customerId = :customerId AND createdAt > :startTime AND businessId = :businessId ORDER BY createdAt ASC")
    fun listTransactions(customerId: String, startTime: Timestamp, businessId: String): Observable<List<Transaction>>

    @Query("select * from `transaction` WHERE customerId = :customerId AND businessId = :businessId ORDER BY createdAt DESC")
    fun listTransactions(customerId: String, businessId: String): Observable<List<Transaction>>

    @Query("select * from `transaction` WHERE customerId = :customerId AND isDeleted == 0 AND state != 0 AND businessId = :businessId ORDER BY billDate DESC")
    fun listNonDeletedTransactionsByBillDate(customerId: String, businessId: String): Observable<List<Transaction>>

    @Query("select * from `transaction` WHERE businessId = :businessId and isDirty = :isDirty")
    fun listDirtyTransactions(isDirty: Boolean, businessId: String): Observable<List<Transaction>>

    @Query("select * from CustomerWithTransactionsInfo where id = :customerId LIMIT 1")
    fun getCustomer(customerId: String): Observable<CustomerWithTransactionsInfo>

    // Higher priority to Clean over Dirty and Immutable customer profiles
    @Query("select * from CustomerWithTransactionsInfo where businessId = :businessId and mobile = :mobile order by customerSyncStatus asc LIMIT 1")
    fun getCustomerByMobile(mobile: String, businessId: String): Single<CustomerWithTransactionsInfo>

    @Query("select * from CustomerWithTransactionsInfo where businessId = :businessId ORDER BY description")
    fun listCustomers(businessId: String): Observable<List<CustomerWithTransactionsInfo>>

    @Query("select * from CustomerWithTransactionsInfo where businessId = :businessId ORDER BY lastPayment desc")
    fun listCustomersByLastPayment(businessId: String): Observable<List<CustomerWithTransactionsInfo>>

    @Query("select * from CustomerWithTransactionsInfo WHERE businessId = :businessId AND status == 1 ORDER BY description")
    fun listActiveCustomers(businessId: String): Observable<List<CustomerWithTransactionsInfo>>

    @Query("select id from CustomerWithTransactionsInfo WHERE businessId = :businessId AND status == 1 ORDER BY description")
    fun listActiveCustomersIds(businessId: String): Observable<List<String>>

    @Query("select id from Customer WHERE isLiveSales == 1 AND businessId = :businessId")
    fun getLiveSalesCustomerId(businessId: String): Single<String>

    @Query("select * from CustomerWithTransactionsInfo WHERE businessId = :businessId AND status == 1 AND mobile IS NOT NULL AND balance < -1000 AND balance > -10000000 ORDER BY lastPayment asc")
    fun getDefaulters(businessId: String): Observable<List<CustomerWithTransactionsInfo>>

    @Query("select * from CustomerWithTransactionsInfo WHERE businessId = :businessId AND status == 1 AND balance < 0 ORDER BY balance asc")
    fun getCustomersWithBalanceDue(businessId: String): Observable<List<CustomerWithTransactionsInfo>>

    @Query("SELECT count(*) FROM Customer WHERE businessId = :businessId")
    fun getCustomerCount(businessId: String): Observable<Int>

    @Query("SELECT count(*) FROM CustomerWithTransactionsInfo where businessId = :businessId and status == 1")
    fun getActiveCustomerCount(businessId: String): Observable<Long>

    @Query("UPDATE customer SET lastViewTime=:lastViewTime, newActivityCount = 0  WHERE id = :customerId")
    fun markActivityAsSeen(customerId: String, lastViewTime: Timestamp): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCustomer(customer: Customer): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putCustomers(customer: List<Customer>)

    @Query("SELECT * FROM Customer WHERE businessId = :businessId")
    fun getCustomers(businessId: String): Observable<List<Customer>>

    @androidx.room.Transaction
    fun resetCustomerList(customers: List<Customer>) {
        // Since we allow adding customers offline with CoreSdk, this makes no sense
        // deleteAllCustomers()

        // Just make do with overwriting the customers
        putCustomers(customers)
    }

    @Query("delete from Customer where businessId = :businessId")
    fun clearCustomerTable(businessId: String): Completable

    @Query("delete from Customer")
    fun deleteCustomerTable(): Completable

    @Query("select count(*) from Customer where id = :id")
    fun isCustomerPresent(id: String): Single<Int>

    @Query("delete from Customer where id = :accountId")
    fun deleteCustomer(accountId: String): Completable

    @Query("delete from `transaction` where customerId = :accountId")
    fun deleteLocalTransactionsForCustomer(accountId: String): Completable

    @Query("UPDATE customer SET description=:description  WHERE id = :customerId")
    fun updateCustomerDescription(description: String, customerId: String): Completable

    @Query("select count(*) FROM `transaction` WHERE businessId = :businessId")
    fun getAllTransactionsCount(businessId: String): Single<Int>

    @Query("select count(*) FROM `transaction` WHERE businessId = :businessId and updatedAt <= :time and isDirty = 0")
    fun getSyncedTransactionsCountTillGivenUpdatedTime(time: Timestamp, businessId: String): Single<Int>

    @Query("select count(*) FROM `transaction` where businessId = :businessId and type = :type")
    fun getTransactionCountByType(type: Int, businessId: String): Single<Int>

    @Query("select * from `transaction` where businessId = :businessId order by createdAt ASC limit 1")
    fun getFirstTransaction(businessId: String): Single<Transaction>

    @Query("select * from `transaction`where businessId = :businessId order by createdAt DESC limit 1")
    fun getLastTransaction(businessId: String): Single<Transaction>

    @Query("select * from `transaction` where  customerId = :customerId and createdByCustomer = 1 and deletedByCustomer = 0 and businessId = :businessId order by createdAt DESC limit 1")
    fun getLatestTransactionAddedByCustomer(customerId: String, businessId: String): Single<Transaction>

    @Query("select * from `transaction` where  customerId = :customerId and deletedByCustomer = 0  order by createdAt DESC limit 1")
    fun getLatestTransaction(customerId: String): Single<Transaction>

    @Query("UPDATE customer SET addTransactionRestricted=:isDenied  WHERE id = :accountID")
    fun updateCustomerAddTransactionPermission(accountID: String, isDenied: Boolean): Completable

    @Query("update `transaction` set amount = :amount where id = :transactionId")
    fun updateAmount(transactionId: String, amount: Long)

    @androidx.room.Transaction
    fun updateTransactionAmount(
        amount: Long,
        command: Command,
    ) {
        updateAmount(command.transactionId, amount)
        addTransactionCommand(command)
    }

    @Query("SELECT id FROM `transaction` WHERE businessId = :businessId AND createdAt >= :startTime AND createdAt <= :endTime")
    fun getTransactionsIdsByCreatedTime(
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Single<List<String>>

    @androidx.room.Transaction
    fun markTransactionDirtyAndInsertCommand(transactionId: String, command: Command) {
        markTransactionDirty(transactionId, true)
        insertCommand(command)
    }

    @Query("SELECT * FROM `transaction` WHERE businessId = :businessId AND images LIKE :imageId")
    fun getDbTransactionsWithImageId(imageId: String, businessId: String): Single<List<Transaction>>

    @Query("SELECT * FROM Command WHERE businessId = :businessId AND value LIKE :imageId")
    fun getDbCommandsWithImageId(imageId: String, businessId: String): Single<List<Command>>

    @androidx.room.Transaction
    fun updateTransactionImagesAndCommandValues(
        transactionIdToImagesList: List<Pair<String, String>>,
        commandIdToValueList: List<Pair<Int, String>>,
    ) {
        transactionIdToImagesList.forEach { updateImagesInTransaction(it.first, it.second) }
        commandIdToValueList.forEach { updateValueInCommand(it.first, it.second) }
    }

    @Query("update `transaction` set images = :transactionImages where id = :transactionId")
    fun updateImagesInTransaction(transactionId: String, transactionImages: String)

    @Query("update Command set value = :commandValue where id = :commandId")
    fun updateValueInCommand(commandId: Int, commandValue: String)

    @Query("SELECT * FROM SuggestedCustomerIdsForAddTransaction where businessId = :businessId")
    fun getSuggestedCustomerIdsForAddTransaction(businessId: String): Single<List<SuggestedCustomerIdsForAddTransaction>>

    @Query("DELETE FROM SuggestedCustomerIdsForAddTransaction WHERE businessId = :businessId")
    fun clearSuggestedCustomerIdsForAddTransaction(businessId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSuggestedCustomerIdsForAddTransaction(vararg ids: SuggestedCustomerIdsForAddTransaction)

    @androidx.room.Transaction
    fun replaceSuggestedCustomerIdsForAddTransaction(
        vararg ids: SuggestedCustomerIdsForAddTransaction,
        businessId: String,
    ) {
        clearSuggestedCustomerIdsForAddTransaction(businessId)
        insertSuggestedCustomerIdsForAddTransaction(*ids)
    }

    @Query("select * from `transaction` where customerId = :customerId AND collectionId IS NOT NULL")
    fun listOnlineTransactions(customerId: String): Observable<List<Transaction>>

    @Query(
        """
        SELECT
        sum(balance) as totalBalanceDue,

        count(*) as totalCustomers,

        (SELECT count(*)
        From CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL AND businessId =:businessId
        AND ((Case when lastPayment > 0 then lastPayment else
        (select createdAt from `transaction` b where a.id == b.customerId And a.businessId = b.businessId order by createdAt ASC limit 1)
        end)) < (1000 * strftime('%s', datetime('now', :defaulterSince)))
        AND ((lastReminderSendTime == 0 OR lastReminderSendTime is Null) OR lastReminderSendTime <= (1000 * strftime('%s', datetime('now', '-1 day'))))) as countNumberOfCustomers

        From (SELECT * from CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL AND businessId =:businessId
        AND (
        (lastPayment > 0 AND lastPayment < (1000 * strftime('%s', datetime('now', :defaulterSince))) )
        OR
        (lastPayment == 0 AND (select createdAt from `transaction` b
        where a.id == b.customerId AND a.businessId = b.businessId
        order by createdAt ASC limit 1)
        < (1000 * strftime('%s', datetime('now', :defaulterSince))) )
        ))
        """
    )
    fun getDefaultersDataForBanner(defaulterSince: String, businessId: String): Flow<BulkReminderDbInfo>

    @Query(
        """
        SELECT id, businessId, description, profileImage, balance, lastPayment, lastReminderSendTime, reminderMode,

        (Case when lastPayment > 0 then lastPayment else
        (select createdAt from `transaction` b where a.id == b.customerId And a.businessId = b.businessId order by createdAt ASC limit 1)
        end) as dueSinceTime

        From CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL AND businessId =:businessId
        AND dueSinceTime < (1000 * strftime('%s', datetime('now', :defaulterSince)))
        AND ((lastReminderSendTime == 0 OR lastReminderSendTime is Null) OR lastReminderSendTime <= (1000 * strftime('%s', datetime('now', '-1 day'))))
        """
    )
    fun getDefaultersForPendingReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<CoreDbReminderProfile>>

    @Query(
        """
        SELECT id, businessId, description, profileImage, balance, lastPayment, lastReminderSendTime, reminderMode,

        (Case when lastPayment > 0 then lastPayment else
        (select createdAt from `transaction` b where a.id == b.customerId AND a.businessId = b.businessId order by createdAt ASC limit 1)
        end) as dueSinceTime

        From CustomerWithTransactionsInfo a
        where balance < 0 AND mobile IS NOT NULL AND businessId =:businessId
        AND dueSinceTime < (1000 * strftime('%s', datetime('now', :defaulterSince)))
        AND lastReminderSendTime >= (1000 * strftime('%s', datetime('now', '-1 day')))
        """
    )
    fun getDefaultersForTodaysReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<CoreDbReminderProfile>>

    @Query("UPDATE customer SET lastReminderSendTime =:lastReminderSentTime  WHERE id = :customerId")
    suspend fun updateLastReminderSendTime(customerId: String, lastReminderSentTime: Timestamp)

    @Query("""Select id as customerId, lastReminderSendTime from CustomerWithTransactionsInfo where businessId =:businessId AND id in (:customerIds)""")
    suspend fun getDirtyLastReminderSendTime(
        customerIds: List<String>,
        businessId: String,
    ): List<CoreLastReminderSendTime>
}
