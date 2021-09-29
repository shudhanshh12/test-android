package `in`.okcredit.merchant.core.store

import `in`.okcredit.merchant.core.Command
import `in`.okcredit.merchant.core.Command.*
import `in`.okcredit.merchant.core.Command.CommandType.*
import `in`.okcredit.merchant.core._di.Core
import `in`.okcredit.merchant.core.common.CoreException
import `in`.okcredit.merchant.core.common.CoreUtils
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.common.TimestampUtils
import `in`.okcredit.merchant.core.common.toTimestamp
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.DIRTY
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.IMMUTABLE
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionImage
import `in`.okcredit.merchant.core.model.bulk_reminder.CoreLastReminderSendTime
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import `in`.okcredit.merchant.core.store.database.CoreDatabaseDao
import `in`.okcredit.merchant.core.store.database.CoreDbReminderProfile
import `in`.okcredit.merchant.core.store.database.CustomerWithTransactionsInfo
import `in`.okcredit.merchant.core.store.database.SuggestedCustomerIdsForAddTransaction
import androidx.room.EmptyResultSetException
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.extensions.encloseWithPercentageSymbol
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import `in`.okcredit.merchant.core.store.database.Command as DbCommand
import `in`.okcredit.merchant.core.store.database.Customer as DbCustomer

class CoreLocalSourceImpl @Inject constructor(
    private val coreDatabaseDao: Lazy<CoreDatabaseDao>,
    @Core private val moshi: Lazy<Moshi>,
) : CoreLocalSource {

    @ExperimentalCoroutinesApi
    @Suppress("RemoveExplicitTypeArguments")
    override suspend fun createOfflineCustomer(
        description: String,
        mobile: String?,
        profileImage: String?,
        businessId: String,
    ): Customer = (
        mobile
            ?.takeIf { it.isNotBlank() }
            ?.let { coreDatabaseDao.get().getCustomersByMobile(it, businessId) }
            ?.firstOrNull { it.status == 2 }
            ?.id
            ?.also { coreDatabaseDao.get().deleteAccount(it) }

            ?: flow<String> {
                // Ensure we don't select a uuid that already exists in customer table
                while (true) {
                    val uuid = CoreUtils.generateRandomId()
                    if (isCustomerPresent(uuid).await()) {
                        continue
                    }
                    emit(uuid)
                    break
                }
            }.first()
        )
        .let { id ->
            val timeNow = DateTimeUtils.currentDateTime().millis.toTimestamp()
            Customer(
                id = id,
                customerSyncStatus = DIRTY.code,
                status = 1,
                mobile = mobile,
                description = description,
                createdAt = timeNow,
                txnStartTime = timeNow,
                balance = 0,
                transactionCount = 0,
                profileImage = profileImage,
                registered = false,
                txnAlertEnabled = false,
                isLiveSales = false,
                lastActivityMetaInfo = null,
                lastAmount = null,
                restrictContactSync = true
            )
        }
        .also {
            val command = CreateCustomerDirty(it.id)

            coreDatabaseDao.get().createOfflineCustomer(
                customer = it.toDbCustomer(businessId),
                command = command.toDbCommand(businessId)
            )
        }

    override suspend fun listDirtyCustomerCommands(pageSize: Int, businessId: String): List<Command> {
        return coreDatabaseDao.get().listDirtyCustomerCommands(pageSize, businessId = businessId).toCommandList()
    }

    override suspend fun listImmutableCustomerCommands(offset: Int, pageSize: Int, businessId: String): List<Command> {
        return coreDatabaseDao.get().listImmutableCustomerCommands(offset, pageSize, businessId = businessId)
            .toCommandList()
    }

    override suspend fun getCustomerSus(customerId: String): Customer {
        return coreDatabaseDao.get().getCustomerSus(customerId).toCustomer()
    }

    override suspend fun putCustomerSus(customer: Customer, businessId: String) {
        coreDatabaseDao.get().putCustomerSus(customer.toDbCustomer(businessId))
    }

    override suspend fun putCustomerSus(customers: List<Customer>, businessId: String) {
        coreDatabaseDao.get().putCustomerSus(customers.toDbCustomerList(businessId).toTypedArray())
    }

    override suspend fun deleteImmutableAccount(customerId: String) {
        coreDatabaseDao.get().deleteAccount(accountId = customerId)
    }

    override suspend fun getTransactionCountForCustomer(customerId: String): Int {
        return coreDatabaseDao.get().getTransactionCountForCustomer(customerId)
    }

    override suspend fun replaceCustomerId(oldId: String, newId: String) {
        coreDatabaseDao.get().replaceCustomerId(oldId, newId)
    }

    override suspend fun updateCustomerSyncStatus(customerId: String, customerSyncStatus: CustomerSyncStatus) {
        coreDatabaseDao.get().updateCustomerSyncStatus(customerId, customerSyncStatus.code)
    }

    override suspend fun getCommandForCustomerId(customerId: String, businessId: String): Command? {
        return coreDatabaseDao.get().getCommandForCustomerId(customerId, businessId)?.toCommand()
    }

    override suspend fun updateCustomerCommandType(commandId: String, type: CommandType) {
        coreDatabaseDao.get().updateCustomerCommandType(commandId, type)
    }

    override suspend fun getCustomersByMobile(mobile: String, businessId: String): List<Customer> {
        return coreDatabaseDao.get().getCustomersByMobile(mobile, businessId).toCustomerList()
    }

    override fun getUnSyncedCustomersCount(businessId: String): Flow<Int> {
        return coreDatabaseDao.get().getUnSyncedCustomersCount(businessId)
    }

    override fun getCommandsCount(type: List<CommandType>, businessId: String): Flow<Int> {
        return coreDatabaseDao.get().getCommandsCount(type, businessId)
    }

    override suspend fun getIsBlocked(businessId: String, customerId: String): Boolean {
        return coreDatabaseDao.get().getCustomerState(businessId, customerId) == Customer.State.BLOCKED.code
    }

    override suspend fun getIsAddTransactionRestricted(businessId: String, customerId: String): Boolean {
        return coreDatabaseDao.get().getIsAddTransactionRestricted(businessId, customerId)
    }

    // ---------------------------------

    override fun createTransaction(
        transaction: Transaction,
        command: CreateTransaction,
        businessId: String,
    ): Completable {
        return Completable.fromAction {
            coreDatabaseDao.get().createTransaction(
                transaction.toDbTransaction(businessId),
                command.toDbCommand(businessId)
            )
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun listTransactions(businessId: String): Observable<List<Transaction>> {
        return coreDatabaseDao.get().listTransactions(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { transaction -> transaction.toTransaction() } }
    }

    override fun listTransactionsCommandsForCleanCustomers(count: Int, businessId: String): Observable<List<Command>> {
        return coreDatabaseDao.get().getCustomersIds(listOf(DIRTY.code, IMMUTABLE.code), businessId)
            .flatMapObservable {
                coreDatabaseDao.get().listTransactionsCommandsWithoutOfflineCustomers(
                    count = count,
                    offlineCustomers = it,
                    businessId = businessId
                )
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCommandList() }
    }

    override fun listTransactionsCommandsForCleanCustomers(businessId: String): Observable<List<Command>> {
        return coreDatabaseDao.get().getCustomersIds(listOf(DIRTY.code, IMMUTABLE.code), businessId)
            .flatMapObservable {
                coreDatabaseDao.get()
                    .listTransactionsCommandsWithoutOfflineCustomers(offlineCustomers = it, businessId = businessId)
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCommandList() }
    }

    override fun updateTransactionNote(command: UpdateTransactionNote, businessId: String): Completable {
        return Completable.fromAction {
            coreDatabaseDao.get().updateTransactionNote(
                command.note,
                command.toDbCommand(businessId)
            )
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun updateTransactionAmount(command: UpdateTransactionAmount, businessId: String): Completable {
        return Completable.fromAction {
            coreDatabaseDao.get().updateTransactionAmount(
                command.amount,
                command.toDbCommand(businessId)
            )
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun isTransactionPresent(id: String): Single<Boolean> {
        return coreDatabaseDao.get().isTransactionPresent(id)
            .flatMap {
                Single.just(it == 1)
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun putTransaction(transaction: Transaction, businessId: String): Completable {
        return putTransactions(listOf(transaction), businessId)
    }

    override fun putTransactions(transactionList: List<Transaction>, businessId: String): Completable {
        return Completable.fromAction {
            val list = transactionList.map { it.toDbTransaction(businessId) }.toTypedArray()
            coreDatabaseDao.get().insertTransaction(*list)
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun lastUpdatedTransactionTime(businessId: String): Single<Timestamp> =
        coreDatabaseDao.get().lastUpdatedTransactionTime(businessId)
            .onErrorReturn { Timestamp(0) }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun clearTransactionTableForBusiness(businessId: String): Completable =
        coreDatabaseDao.get().clearTransactionTable(businessId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun clearTransactionTable(): Completable =
        coreDatabaseDao.get().deleteTransactionTable().subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun deleteCommands(ids: List<String>): Completable =
        coreDatabaseDao.get().deleteCommands(ids.toTypedArray()).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun deleteTransaction(command: DeleteTransaction, businessId: String): Completable {
        return Completable.fromAction {
            coreDatabaseDao.get().deleteTransaction(command.toDbCommand(businessId))
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun clearCommandTableForBusiness(businessId: String): Completable =
        coreDatabaseDao.get().clearCommandTable(businessId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun clearCommandTable(): Completable =
        coreDatabaseDao.get().deleteCommandTable().subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun markTransactionDirty(ids: List<String>, isDirty: Boolean): Completable =
        coreDatabaseDao.get().markTransactionsDirty(ids.toTypedArray(), isDirty)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun replaceTransactionId(oldId: String, newId: String): Completable {
        return Completable.fromAction {
            coreDatabaseDao.get().replaceTransactionId(oldId, newId)
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getTransaction(transactionId: String): Observable<Transaction> =
        coreDatabaseDao.get().getTransaction(transactionId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toTransaction() }

    override fun updateTransactionImagesAndInsertCommands(
        images: List<TransactionImage>,
        transactionId: String,
        commands: List<Command>,
        businessId: String,
    ): Completable {
        return Completable.fromAction {
            coreDatabaseDao.get().updateTransactionImagesAndInsertCommands(
                convertImageUrlListToImageUrlString(images),
                transactionId,
                commands.map { it.toDbCommand(businessId) }
            )
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getTransactionByCollectionId(collectionId: String, businessId: String): Observable<Transaction> {
        return coreDatabaseDao.get().getTransactionByCollectionId(collectionId, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toTransaction() }
    }

    override fun getAllTransactionsCount(businessId: String): Single<Int> {
        return coreDatabaseDao.get().getAllTransactionsCount(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getTransactionsCountTillGivenUpdatedTime(time: Timestamp, businessId: String): Single<Int> {
        return coreDatabaseDao.get().getSyncedTransactionsCountTillGivenUpdatedTime(time, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun isTransactionForCollectionPresent(collectionId: String, businessId: String): Single<Boolean> {
        return coreDatabaseDao.get().isTransactionForCollectionPresent(collectionId, businessId)
            .flatMap { Single.just(it == 1) }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun listActiveTransactionsBetweenBillDate(
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreDatabaseDao.get().listActiveTransactionsBetweenBillDate(startTime, endTime, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { dbTransaction -> dbTransaction.toTransaction() } }
    }

    override fun listActiveTransactionsBetweenBillDate(
        customerId: String,
        customerTxnTime: Timestamp,
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreDatabaseDao.get()
            .listActiveTransactionsBetweenBillDate(customerId, customerTxnTime, startTime, endTime, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { dbTransaction -> dbTransaction.toTransaction() } }
    }

    override fun listTransactionsSortedByBillDate(
        customerId: String,
        startTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreDatabaseDao.get().listTransactionsSortedByBillDate(customerId, startTime, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { dbTransaction -> dbTransaction.toTransaction() } }
    }

    override fun listTransactions(
        customerId: String,
        startTime: Timestamp,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreDatabaseDao.get().listTransactions(customerId, startTime, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { dbTransaction -> dbTransaction.toTransaction() } }
    }

    override fun listTransactions(customerId: String, businessId: String): Observable<List<Transaction>> {
        return coreDatabaseDao.get().listTransactions(customerId = customerId, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { dbTransaction -> dbTransaction.toTransaction() } }
    }

    override fun listNonDeletedTransactionsByBillDate(
        customerId: String,
        businessId: String,
    ): Observable<List<Transaction>> {
        return coreDatabaseDao.get().listNonDeletedTransactionsByBillDate(customerId, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { dbTransaction -> dbTransaction.toTransaction() } }
    }

    override fun listDirtyTransactions(isDirty: Boolean, businessId: String): Observable<List<Transaction>> {
        return coreDatabaseDao.get().listDirtyTransactions(isDirty, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { dbTransaction -> dbTransaction.toTransaction() } }
    }

    override fun getCustomer(customerId: String): Observable<Customer> {
        return coreDatabaseDao.get().getCustomer(customerId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCustomer() }
            .distinctUntilChanged()
    }

    override fun getCustomerByMobile(mobile: String, businessId: String): Single<Customer> {
        return coreDatabaseDao.get().getCustomerByMobile(mobile, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCustomer() }
            .onErrorResumeNext { throwable: Throwable? ->
                if (throwable is EmptyResultSetException) {
                    Single.error<Customer>(NoSuchElementException())
                } else {
                    Single.error<Customer>(RuntimeException(throwable))
                }
            }
    }

    override fun listCustomers(businessId: String): Observable<List<Customer>> {
        return coreDatabaseDao.get().listCustomers(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCustomerList() }
    }

    override fun listCustomersByLastPayment(businessId: String): Observable<List<Customer>> {
        return coreDatabaseDao.get().listCustomersByLastPayment(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCustomerList() }
    }

    override fun listActiveCustomers(businessId: String): Observable<List<Customer>> {
        return coreDatabaseDao.get().listActiveCustomers(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCustomerList() }
    }

    override fun listActiveCustomersIds(businessId: String): Observable<List<String>> {
        return coreDatabaseDao.get().listActiveCustomersIds(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getCustomerCount(businessId: String): Observable<Int> {
        return coreDatabaseDao.get().getCustomerCount(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getActiveCustomerCount(businessId: String): Observable<Long> {
        return coreDatabaseDao.get().getActiveCustomerCount(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun markActivityAsSeen(customerId: String): Completable {
        return coreDatabaseDao.get().markActivityAsSeen(customerId, TimestampUtils.currentTimestamp())
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    private fun isCustomerPresent(customerId: String): Single<Boolean> =
        coreDatabaseDao.get().isCustomerPresent(customerId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it == 1 }

    override fun putCustomer(customer: Customer, businessId: String): Completable {
        return isCustomerPresent(customer.id)
            .flatMap { present ->
                if (present) {
                    coreDatabaseDao.get().getCustomer(customer.id)
                        .firstOrError()
                        .flatMap { existingCustomer ->
                            if (existingCustomer.lastViewTime?.epoch != 0L) {
                                customer.lastViewTime = existingCustomer.lastViewTime
                            } else {
                                customer.lastViewTime =
                                    (System.currentTimeMillis() - 10000).toTimestamp() // 10 secs
                            }
                            Single.just(customer)
                        }
                } else {
                    customer.lastViewTime = (System.currentTimeMillis() - 10000).toTimestamp() // 10 secs
                    Single.just(customer)
                }
            }
            .flatMapCompletable {
                coreDatabaseDao.get().putCustomer(it.toDbCustomer(businessId))
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun resetCustomerList(customers: List<Customer>, businessId: String): Completable {
        return coreDatabaseDao.get().getCustomers(businessId)
            .onErrorReturn { ArrayList() }
            .firstOrError()
            .flatMapCompletable { existingCustomers: List<DbCustomer> ->
                Completable.fromAction {
                    val map = existingCustomers.associateBy { it.id }
                    customers.forEach {
                        if (map.containsKey(it.id)) {
                            it.lastViewTime = map[it.id]!!.lastViewTime
                        }
                    }
                    coreDatabaseDao.get().resetCustomerList(customers.toDbCustomerList(businessId))
                }
                    .subscribeOn(ThreadUtils.database())
                    .observeOn(ThreadUtils.worker())
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun clearCustomerTableForBusiness(businessId: String): Completable =
        coreDatabaseDao.get().clearCustomerTable(businessId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun clearCustomerTable(): Completable =
        coreDatabaseDao.get().deleteCustomerTable().subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun deleteCustomer(accountId: String) =
        coreDatabaseDao.get().deleteCustomer(accountId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun deleteLocalTransactionsForCustomer(accountId: String) =
        coreDatabaseDao.get().deleteLocalTransactionsForCustomer(accountId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun updateCustomerDescription(description: String, customerId: String) =
        coreDatabaseDao.get().updateCustomerDescription(description, customerId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    override fun getFirstTransaction(businessId: String) = coreDatabaseDao.get().getFirstTransaction(businessId)
        .subscribeOn(ThreadUtils.database())
        .observeOn(ThreadUtils.worker())
        .map { it.toTransaction() }

    override fun getLastTransaction(businessId: String) = coreDatabaseDao.get().getLastTransaction(businessId)
        .subscribeOn(ThreadUtils.database())
        .observeOn(ThreadUtils.worker())
        .map { it.toTransaction() }

    override fun getLatestTransactionAddedByCustomer(customerId: String, businessId: String) =
        coreDatabaseDao.get().getLatestTransactionAddedByCustomer(customerId, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker()).map { it.toTransaction() }

    override fun getLatestTransaction(customerId: String) =
        coreDatabaseDao.get().getLatestTransaction(customerId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker()).map { it.toTransaction() }

    override fun updateCustomerAddTransactionPermission(accountID: String, isDenied: Boolean): Completable {
        return coreDatabaseDao.get().updateCustomerAddTransactionPermission(accountID, isDenied)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getTransactionsIdsByCreatedTime(
        startTime: Timestamp,
        endTime: Timestamp,
        businessId: String,
    ): Single<List<String>> =
        coreDatabaseDao.get().getTransactionsIdsByCreatedTime(startTime, endTime, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())

    private fun isCommandTypeForTransactionIdPresent(transactionId: String, commandType: CommandType): Boolean {
        return coreDatabaseDao.get().isCommandTypeForTransactionIdPresent(transactionId, commandType) > 0
    }

    override fun markTransactionDirtyAndInsertCommandIfNotPresent(
        transactionId: String,
        command: CreateTransaction,
        businessId: String,
    ): Completable {
        return Completable.fromAction {
            synchronized(coreDatabaseDao) {
                if (isCommandTypeForTransactionIdPresent(transactionId, command.commandType).not()) {
                    coreDatabaseDao.get()
                        .markTransactionDirtyAndInsertCommand(transactionId, command.toDbCommand(businessId))
                }
            }
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getSuggestedCustomerIdsForAddTransaction(businessId: String): Single<List<String>> {
        return coreDatabaseDao.get().getSuggestedCustomerIdsForAddTransaction(businessId)
            .map { suggestionList -> suggestionList.map { it.id } }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun replaceSuggestedCustomerIdsForAddTransaction(ids: List<String>, businessId: String): Completable {
        return Completable.fromAction {
            coreDatabaseDao.get().replaceSuggestedCustomerIdsForAddTransaction(
                *ids.map { SuggestedCustomerIdsForAddTransaction(it, businessId) }.toTypedArray(),
                businessId = businessId
            )
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getTransactionCountByType(type: Int, businessId: String) =
        coreDatabaseDao.get().getTransactionCountByType(type, businessId)

    override fun getDefaulters(businessId: String): Observable<List<Customer>> {
        return coreDatabaseDao.get().getDefaulters(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCustomerList() }
    }

    override fun getCustomersWithBalanceDue(businessId: String): Observable<List<Customer>> {
        return coreDatabaseDao.get().getCustomersWithBalanceDue(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.toCustomerList() }
    }

    override fun listOnlineTransactions(customerId: String): Observable<List<Transaction>> {
        return coreDatabaseDao.get().listOnlineTransactions(customerId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { it.map { transaction -> transaction.toTransaction() } }
    }

    override fun getTransactionIdForCollection(collectionId: String, businessId: String): Single<String> {
        return coreDatabaseDao.get().getTransactionIdByCollectionId(collectionId, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getLiveSalesCustomerId(businessId: String): Single<String> {
        return coreDatabaseDao.get().getLiveSalesCustomerId(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getDbTransactionsWithImageId(
        imageId: String,
        businessId: String,
    ): Single<List<`in`.okcredit.merchant.core.store.database.Transaction>> {
        return coreDatabaseDao.get().getDbTransactionsWithImageId(imageId.encloseWithPercentageSymbol(), businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getDbCommandsWithImageId(
        imageId: String,
        businessId: String,
    ): Single<List<`in`.okcredit.merchant.core.store.database.Command>> {
        return coreDatabaseDao.get().getDbCommandsWithImageId(imageId.encloseWithPercentageSymbol(), businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun updateTransactionImagesAndCommandValues(
        transactionIdToImagesList: List<Pair<String, String>>,
        commandIdToValueList: List<Pair<Int, String>>,
    ): Completable {
        return Completable.fromAction {
            synchronized(coreDatabaseDao) {
                coreDatabaseDao.get()
                    .updateTransactionImagesAndCommandValues(transactionIdToImagesList, commandIdToValueList)
            }
        }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getDefaultersDataForBanner(defaulterSince: String, businessId: String): Flow<BulkReminderDbInfo> {
        return coreDatabaseDao.get().getDefaultersDataForBanner(defaulterSince, businessId)
    }

    override fun getDefaultersForPendingReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<CoreDbReminderProfile>> {
        return coreDatabaseDao.get().getDefaultersForPendingReminders(defaulterSince, businessId)
    }

    override fun getDefaultersForTodaysReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<CoreDbReminderProfile>> {
        return coreDatabaseDao.get().getDefaultersForTodaysReminders(defaulterSince, businessId)
    }

    override suspend fun updateLastReminderSendTime(
        customerId: String,
        lastReminderSentTime: Timestamp,
        businessId: String,
    ) {
        withContext(Dispatchers.IO) {
            coreDatabaseDao.get().updateLastReminderSendTime(customerId, lastReminderSentTime)
        }
    }

    override suspend fun getDirtyLastReminderSendTime(
        customerIds: List<String>,
        businessId: String,
    ): List<CoreLastReminderSendTime> {
        return withContext(Dispatchers.IO) {
            coreDatabaseDao.get().getDirtyLastReminderSendTime(customerIds, businessId)
        }
    }

    private fun Transaction.toDbTransaction(businessId: String): `in`.okcredit.merchant.core.store.database.Transaction {
        return `in`.okcredit.merchant.core.store.database.Transaction(
            id = this.id,
            type = this.type.code,
            customerId = this.customerId,
            amount = this.amount,
            collectionId = this.collectionId,
            images = convertImageUrlListToImageUrlString(this.images),
            note = this.note,
            createdAt = this.createdAt,
            isDeleted = this.isDeleted,
            deleteTime = this.deleteTime,
            isDirty = this.isDirty,
            billDate = this.billDate,
            updatedAt = this.updatedAt,
            smsSent = this.smsSent,
            createdByCustomer = this.createdByCustomer,
            deletedByCustomer = this.deletedByCustomer,
            inputType = this.inputType,
            voiceId = this.voiceId,
            state = this.state.code,
            category = this.category.code,
            amountUpdated = this.amountUpdated,
            amountUpdatedAt = this.amountUpdatedAt,
            businessId = businessId
        )
    }

    private fun `in`.okcredit.merchant.core.store.database.Transaction.toTransaction(): Transaction {
        return Transaction(
            id = this.id,
            type = Transaction.Type.getTransactionType(this.type),
            customerId = this.customerId,
            amount = this.amount,
            collectionId = this.collectionId,
            images = this.images?.let { convertImageUrlStringToImageUrlList(it) } ?: listOf(),
            note = this.note,
            createdAt = this.createdAt,
            isDeleted = this.isDeleted,
            deleteTime = this.deleteTime,
            isDirty = this.isDirty,
            billDate = this.billDate,
            updatedAt = this.updatedAt,
            smsSent = this.smsSent,
            createdByCustomer = this.createdByCustomer,
            deletedByCustomer = this.deletedByCustomer,
            inputType = this.inputType,
            voiceId = this.voiceId,
            state = Transaction.State.getTransactionState(this.state),
            category = Transaction.Category.getTransactionCategory(
                this.category
            ),
            amountUpdated = this.amountUpdated,
            amountUpdatedAt = this.amountUpdatedAt
        )
    }

    private fun Command.toDbCommand(businessId: String): DbCommand {
        return DbCommand(
            commandId = this.id,
            type = this.commandType,
            value = getValue(this),
            timestamp = this.timestamp,
            transactionId = this.transactionId,
            customerId = this.customerId,
            businessId = businessId
        )
    }

    private fun getValue(command: Command): String {
        return when (command) {
            is CreateTransaction -> moshi.get().adapter(CreateTransaction::class.java).toJson(command)
            is UpdateTransactionNote -> moshi.get().adapter(UpdateTransactionNote::class.java).toJson(command)
            is UpdateTransactionAmount -> moshi.get().adapter(UpdateTransactionAmount::class.java).toJson(command)
            is DeleteTransaction -> moshi.get().adapter(DeleteTransaction::class.java).toJson(command)
            is CreateTransactionImage -> moshi.get().adapter(CreateTransactionImage::class.java).toJson(command)
            is DeleteTransactionImage -> moshi.get().adapter(DeleteTransactionImage::class.java).toJson(command)

            is CreateCustomerDirty -> moshi.get().adapter(CreateCustomerDirty::class.java).toJson(command)
            is CreateCustomerImmutable -> moshi.get().adapter(CreateCustomerImmutable::class.java).toJson(command)

            else -> throw CoreException.IllegalArgumentException
        }
    }

    private fun List<DbCommand>.toCommandList(): List<Command> {
        return this.mapNotNull { it.toCommand() }
    }

    private fun DbCommand.toCommand(): Command? {
        val command = getObject(this.type, this.value)
        command?.let {
            it.id = this.commandId
            it.timestamp = this.timestamp
            it.commandType = this.type
            it.transactionId = this.transactionId
            it.customerId = this.customerId
        }
        return command
    }

    private fun getObject(commandType: CommandType, value: String): Command? {
        val commandClass = when (commandType) {
            CREATE_TRANSACTION -> CreateTransaction::class.java
            UPDATE_TRANSACTION_NOTE -> UpdateTransactionNote::class.java
            UPDATE_TRANSACTION_AMOUNT -> UpdateTransactionAmount::class.java
            DELETE_TRANSACTION -> DeleteTransaction::class.java
            CREATE_TRANSACTION_IMAGE -> CreateTransactionImage::class.java
            DELETE_TRANSACTION_IMAGE -> DeleteTransactionImage::class.java

            CREATE_CUSTOMER_DIRTY -> CreateCustomerDirty::class.java
            CREATE_CUSTOMER_IMMUTABLE -> CreateCustomerImmutable::class.java
            else -> return null
        }
        return moshi.get().adapter(commandClass).fromJson(value)
    }

    private fun convertImageUrlStringToImageUrlList(imageUrl: String): List<TransactionImage> =
        stringListAdapter.fromJson(imageUrl) ?: listOf()

    private fun convertImageUrlListToImageUrlString(imageUrls: List<TransactionImage>): String =
        stringListAdapter.toJson(imageUrls)

    private val stringListAdapter: JsonAdapter<List<TransactionImage>> = moshi.get()
        .adapter(Types.newParameterizedType(List::class.java, TransactionImage::class.java))

    private fun CustomerWithTransactionsInfo.toCustomer(): Customer {
        return Customer(
            id = this.id,
            customerSyncStatus = this.customerSyncStatus,
            status = this.status,
            mobile = this.mobile,
            description = this.description,
            createdAt = this.createdAt,
            txnStartTime = this.txnStartTime,
            accountUrl = this.accountUrl,
            balance = this.balance,
            transactionCount = this.transactionCount,
            lastActivity = this.lastActivity,
            lastPayment = this.lastPayment,
            profileImage = this.profileImage,
            address = this.address,
            email = this.email,
            newActivityCount = this.newActivityCount,
            addTransactionPermissionDenied = this.addTransactionRestricted,
            registered = this.registered,
            lastBillDate = this.lastBillDate,
            txnAlertEnabled = this.txnAlertEnabled,
            lang = this.lang,
            reminderMode = this.reminderMode,
            isLiveSales = this.isLiveSales,
            lastActivityMetaInfo = this.lastActivityMetaInfo,
            lastAmount = this.lastAmount,
            lastViewTime = this.lastViewTime,
            blockedByCustomer = this.blockedByCustomer,
            state = Customer.State.getState(this.state),
            restrictContactSync = this.restrictContactSync,
            lastReminderSendTime = this.lastReminderSendTime,
        )
    }

    private fun List<CustomerWithTransactionsInfo>.toCustomerList(): List<Customer> = this.map { it.toCustomer() }

    private fun Customer.toDbCustomer(businessId: String): DbCustomer {
        return DbCustomer(
            id = this.id,
            customerSyncStatus = this.customerSyncStatus,
            status = this.status,
            mobile = this.mobile,
            description = this.description,
            createdAt = this.createdAt,
            txnStartTime = this.txnStartTime,
            accountUrl = this.accountUrl,
            balance = this.balance,
            transactionCount = this.transactionCount,
            lastActivity = this.lastActivity,
            lastPayment = this.lastPayment,
            profileImage = this.profileImage,
            address = this.address,
            email = this.email,
            newActivityCount = this.newActivityCount,
            addTransactionRestricted = this.addTransactionPermissionDenied,
            registered = this.registered,
            lastBillDate = this.lastBillDate,
            txnAlertEnabled = this.txnAlertEnabled,
            lang = this.lang,
            reminderMode = this.reminderMode,
            isLiveSales = this.isLiveSales,
            lastViewTime = this.lastViewTime,
            blockedByCustomer = this.blockedByCustomer,
            state = this.state.code,
            restrictContactSync = this.restrictContactSync,
            businessId = businessId,
            lastReminderSendTime = this.lastReminderSendTime,
        )
    }

    private fun List<Customer>.toDbCustomerList(businessId: String): List<DbCustomer> =
        this.map { it.toDbCustomer(businessId) }
}
