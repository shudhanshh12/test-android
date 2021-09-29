package `in`.okcredit.merchant.core

import `in`.okcredit.accounting_core.contract.QuickAddCustomerModel
import `in`.okcredit.accounting_core.contract.SuggestedCustomerIdsForAddTransaction
import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.fileupload.usecase.IUploadFile
import `in`.okcredit.fileupload.usecase.IUploadFile.Companion.CONTACT_PHOTO
import `in`.okcredit.fileupload.usecase.IUploadFile.Companion.CUSTOMER_PHOTO
import `in`.okcredit.fileupload.usecase.IUploadFile.Companion.RECEIPT_PHOTO
import `in`.okcredit.merchant.core.Command.CommandType.CREATE_CUSTOMER_DIRTY
import `in`.okcredit.merchant.core.Command.CommandType.CREATE_CUSTOMER_IMMUTABLE
import `in`.okcredit.merchant.core.analytics.CoreTracker
import `in`.okcredit.merchant.core.analytics.CoreTracker.DebugType.TRANSACTION_NOT_FOUND
import `in`.okcredit.merchant.core.analytics.CoreTracker.DebugType.TRANSACTION_RECOVERY
import `in`.okcredit.merchant.core.common.CoreException
import `in`.okcredit.merchant.core.common.CoreUtils
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.common.toTimestamp
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import `in`.okcredit.merchant.core.model.TransactionImage
import `in`.okcredit.merchant.core.model.bulk_reminder.CoreLastReminderSendTime
import `in`.okcredit.merchant.core.model.bulk_reminder.LastReminderSendTime
import `in`.okcredit.merchant.core.model.bulk_reminder.SetRemindersApiRequest
import `in`.okcredit.merchant.core.server.CoreRemoteSource
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddCustomerRequestModel
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionModel
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionRequest
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionResponse
import `in`.okcredit.merchant.core.store.CoreLocalSource
import `in`.okcredit.merchant.core.store.database.BulkReminderDbInfo
import `in`.okcredit.merchant.core.store.database.CoreDbReminderProfile
import `in`.okcredit.merchant.core.sync.CoreTransactionSyncer
import `in`.okcredit.merchant.core.sync.SyncCustomer
import `in`.okcredit.merchant.core.sync.SyncCustomers
import `in`.okcredit.merchant.core.sync.SyncTransactionsCommands
import `in`.okcredit.merchant.core.usecase.ClearAllLocalData
import `in`.okcredit.merchant.core.usecase.OfflineAddCustomerAbHelper
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_CORE_SDK_ENABLED
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@Reusable
class CoreSdkImpl @Inject constructor(
    private val localSource: Lazy<CoreLocalSource>,
    private val remoteSource: Lazy<CoreRemoteSource>,
    private val uploadFile: Lazy<IUploadFile>,
    private val tracker: Lazy<CoreTracker>,
    private val syncTransactions: Lazy<CoreTransactionSyncer>,
    private val syncTransactionsTransactionsCommands: Lazy<SyncTransactionsCommands>,
    private val syncCustomer: Lazy<SyncCustomer>,
    private val syncCustomers: Lazy<SyncCustomers>,
    private val rxPref: Lazy<DefaultPreferences>,
    private val schedulerProvider: Lazy<SchedulerProvider>,
    private val offlineAddCustomerAbHelper: Lazy<OfflineAddCustomerAbHelper>,
    private val clearAllLocalData: Lazy<ClearAllLocalData>,
) : CoreSdk, SuggestedCustomerIdsForAddTransaction {
    companion object {
        @NonNls
        const val TAG = "CoreSdk"
    }

    override fun isCoreSdkFeatureEnabled(businessId: String): Single<Boolean> =
        rxPref.get().getBoolean(PREF_BUSINESS_CORE_SDK_ENABLED, Scope.Business(businessId))
            .asObservable()
            .firstOrError()
            .subscribeOn(ThreadUtils.database())

    override fun setCoreSdkFeatureStatus(enable: Boolean, businessId: String) =
        rxCompletable { rxPref.get().set(PREF_BUSINESS_CORE_SDK_ENABLED, enable, Scope.Business(businessId)) }
            .subscribeOn(ThreadUtils.database())

    override fun isCoreSdkFeatureEnabledFlow(businessId: String): Flow<Boolean> =
        rxPref.get().getBoolean(PREF_BUSINESS_CORE_SDK_ENABLED, Scope.Business(businessId))

    override fun clearLocalData(businessId: String?): Completable {
        return if (businessId.isNullOrEmpty()) {
            clearAllLocalData.get().execute()
        } else {
            localSource.get().clearCommandTableForBusiness(businessId)
                .andThen(localSource.get().clearTransactionTableForBusiness(businessId))
                .andThen(localSource.get().clearCustomerTableForBusiness(businessId))
                .andThen(clearCoreSdkFeatureStatus(businessId))
        }
    }

    private fun clearCoreSdkFeatureStatus(businessId: String): Completable {
        return rxCompletable { rxPref.get().remove(PREF_BUSINESS_CORE_SDK_ENABLED, Scope.Business(businessId)) }
            .subscribeOn(ThreadUtils.database())
    }

    // ================================================================================
    // Transaction
    // ================================================================================

    override fun processTransactionCommand(command: Command, businessId: String): Single<Transaction> {
        return when (command) {
            is Command.CreateTransaction -> createTransaction(command, businessId)
            is Command.UpdateTransactionNote -> updateTransactionNote(command, businessId)
            is Command.DeleteTransaction -> deleteTransaction(command, businessId)
            is Command.UpdateTransactionImages -> updateTransactionImages(command, businessId)
            is Command.UpdateTransactionAmount -> updateTransactionAmount(command, businessId)
            else -> Completable.error(CoreException.IllegalArgumentException)
        }
            .andThen(localSource.get().getTransaction(command.transactionId))
            .firstOrError()
    }

    private fun createTransaction(command: Command.CreateTransaction, businessId: String): Completable {
        return Single.defer {
            localSource.get().isTransactionPresent(command.transactionId)
                .flatMap { isAlreadyPresent ->
                    if (isAlreadyPresent.not()) {
                        val images = mutableListOf<TransactionImage>()
                        val uploadFilesCompletable = mutableListOf<Completable>()
                        command.imagesUriList.forEach { filePath ->
                            val imageFile = File(filePath)
                            if (imageFile.exists()) {
                                val url = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + CoreUtils.generateRandomId() + ".jpg"
                                images.add(
                                    TransactionImage(
                                        id = CoreUtils.generateRandomId(),
                                        url = url,
                                        transactionId = command.transactionId,
                                        createdAt = command.timestamp
                                    )
                                )
                                uploadFilesCompletable.add(uploadFile.get().schedule(RECEIPT_PHOTO, url, filePath))
                            } else {
                                tracker.get().trackDebug(
                                    CoreTracker.DebugType.FILE_NOT_FOUND,
                                    "createTransaction filePath: $filePath, transactionId: ${command.transactionId}"
                                )
                            }
                        }
                        val transaction = Transaction(
                            id = command.transactionId,
                            type = command.type,
                            customerId = command.customerId,
                            amount = command.amount,
                            collectionId = null,
                            images = images,
                            note = command.note,
                            createdAt = command.timestamp,
                            billDate = command.billDate ?: command.timestamp,
                            updatedAt = Timestamp(0),
                            inputType = command.inputType,
                            voiceId = command.voiceId
                        )
                        command.transactionImages = transaction.images

                        localSource.get().createTransaction(transaction, command, businessId)
                            .andThen(Completable.concat(uploadFilesCompletable))
                            .andThen(scheduleSyncTransaction(command.commandType.name, businessId))
                            .andThen(Single.just(true))
                    } else {
                        tracker.get().trackDebug(
                            CoreTracker.DebugType.LOCAL_CONFLICT,
                            "$TAG createTransaction transactionId: [${command.transactionId}]"
                        )
                        Timber.d("$TAG transaction id [${command.transactionId}] already present, generating new id")
                        command.transactionId = CoreUtils.generateRandomId()
                        Single.just(false)
                    }
                }
        }
            .repeat()
            .takeUntil { it }
            .ignoreElements()
    }

    private fun updateTransactionNote(command: Command.UpdateTransactionNote, businessId: String): Completable {
        return localSource.get().isTransactionPresent(command.transactionId)
            .flatMapCompletable {
                if (it) {
                    localSource.get().updateTransactionNote(command, businessId)
                        .andThen(scheduleSyncTransaction(command.commandType.name, businessId))
                } else {
                    tracker.get().trackDebug(
                        TRANSACTION_NOT_FOUND,
                        "$TAG getTransaction updateTransactionNote: [${command.transactionId}]"
                    )
                    Completable.error(CoreException.TransactionNotFoundException)
                }
            }
    }

    private fun updateTransactionAmount(command: Command.UpdateTransactionAmount, businessId: String): Completable {
        return localSource.get().isTransactionPresent(command.transactionId)
            .flatMapCompletable { isTransactionPresent ->
                if (isTransactionPresent) {
                    localSource.get().updateTransactionAmount(command, businessId)
                        .andThen(scheduleSyncTransaction(command.commandType.name, businessId))
                } else {
                    tracker.get().trackDebug(
                        TRANSACTION_NOT_FOUND,
                        "$TAG getTransaction updateTransactionAmount: [${command.transactionId}]"
                    )
                    Completable.error(CoreException.TransactionNotFoundException)
                }
            }
    }

    private fun deleteTransaction(command: Command.DeleteTransaction, businessId: String): Completable {
        return localSource.get().isTransactionPresent(command.transactionId)
            .flatMapCompletable {
                if (it) {
                    localSource.get().deleteTransaction(command, businessId)
                        .andThen(scheduleSyncTransaction(command.commandType.name, businessId))
                        .andThen(syncCustomers.get().schedule(businessId))
                } else {
                    tracker.get().trackDebug(
                        TRANSACTION_NOT_FOUND,
                        "$TAG getTransaction updateTransactionNote: [${command.transactionId}]"
                    )
                    Completable.error(CoreException.TransactionNotFoundException)
                }
            }
    }

    private fun updateTransactionImages(command: Command.UpdateTransactionImages, businessId: String): Completable {
        return localSource.get().isTransactionPresent(command.transactionId)
            .flatMapCompletable {
                if (it) {
                    localSource.get().getTransaction(command.transactionId)
                        .firstOrError()
                        .flatMapCompletable { transaction ->
                            val uploadFilesCompletable = mutableListOf<Completable>()
                            val commands = mutableListOf<Command>()

                            val result = mutableListOf<TransactionImage>()
                            val added = command.updatedImagesUriList.toMutableList()
                            transaction.images.forEach { image ->
                                if (command.updatedImagesUriList.contains(image.url)) { // already present
                                    result.add(image)
                                    added.remove(image.url)
                                } else { // deleted
                                    commands.add(Command.DeleteTransactionImage(command.transactionId, image.id))
                                }
                            }
                            added.forEach { filePath -> // added
                                val imageFile = File(filePath)
                                if (imageFile.exists()) {
                                    val url =
                                        IUploadFile.AWS_RECEIPT_BASE_URL + "/" + CoreUtils.generateRandomId() + ".jpg"
                                    val imageId = CoreUtils.generateRandomId()
                                    result.add(
                                        TransactionImage(
                                            id = imageId,
                                            url = url,
                                            transactionId = command.transactionId,
                                            createdAt = command.timestamp
                                        )
                                    )
                                    uploadFilesCompletable.add(uploadFile.get().schedule(RECEIPT_PHOTO, url, filePath))
                                    commands.add(Command.CreateTransactionImage(command.transactionId, imageId, url))
                                } else {
                                    tracker.get().trackDebug(
                                        CoreTracker.DebugType.FILE_NOT_FOUND,
                                        "filePath: $filePath, transactionId: ${command.transactionId}"
                                    )
                                }
                            }

                            Completable.concat(uploadFilesCompletable)
                                .andThen(
                                    localSource.get().updateTransactionImagesAndInsertCommands(
                                        result,
                                        command.transactionId,
                                        commands,
                                        businessId
                                    )
                                )
                                .andThen(scheduleSyncTransaction(command.commandType.name, businessId))
                        }
                } else {
                    tracker.get().trackDebug(
                        TRANSACTION_NOT_FOUND,
                        "$TAG getTransaction updateTransactionNote: [${command.transactionId}]"
                    )
                    Completable.error(CoreException.TransactionNotFoundException)
                }
            }
    }

    override fun syncTransactionsCommands(businessId: String?): Completable {
        return syncTransactionsTransactionsCommands.get().execute(businessId)
    }

    override fun syncTransactions(
        source: String,
        req: BehaviorSubject<SyncState>?,
        isFromSyncScreen: Boolean,
        isFromForceSync: Boolean,
        businessId: String?,
    ): Completable {
        return syncTransactions.get().execute(source, req, isFromSyncScreen, isFromForceSync, businessId)
    }

    override fun scheduleSyncTransactions(source: String, businessId: String): Completable {
        return scheduleSyncTransaction(source, businessId)
    }

    override fun isTransactionPresent(transactionId: String): Single<Boolean> =
        localSource.get().isTransactionPresent(transactionId)

    override fun isTransactionForCollectionPresent(collectionId: String, businessId: String): Single<Boolean> =
        localSource.get().isTransactionForCollectionPresent(collectionId, businessId)

    override fun getTransaction(transactionId: String, businessId: String): Observable<Transaction> {
        return localSource.get().isTransactionPresent(transactionId)
            .flatMapObservable {
                if (it) {
                    localSource.get().getTransaction(transactionId)
                } else {
                    tracker.get().trackDebug(
                        TRANSACTION_NOT_FOUND,
                        "$TAG getTransaction transactionId: [$transactionId]"
                    )
                    remoteSource.get().getTransaction(transactionId, businessId)
                        .flatMapCompletable { transaction ->
                            localSource.get().putTransactions(listOf(transaction), businessId)
                        }
                        .andThen(localSource.get().getTransaction(transactionId))
                }
            }
    }

    override fun getTransactionByCollectionId(collectionId: String, businessId: String): Observable<Transaction> =
        localSource.get().getTransactionByCollectionId(collectionId, businessId)

    override fun getAllTransactionsCount(businessId: String): Single<Int> =
        localSource.get().getAllTransactionsCount(businessId)

    override fun getTransactionCountByType(type: Int, businessId: String) =
        localSource.get().getTransactionCountByType(type, businessId)

    override fun listTransactions(businessId: String): Observable<List<Transaction>> =
        localSource.get().listTransactions(businessId)

    override fun listActiveTransactionsBetweenBillDate(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> =
        localSource.get()
            .listActiveTransactionsBetweenBillDate(startTime.toTimestamp(), endTime.toTimestamp(), businessId)

    override fun listActiveTransactionsBetweenBillDate(
        customerId: String,
        customerTxnTime: Long,
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> =
        localSource.get().listActiveTransactionsBetweenBillDate(
            customerId,
            customerTxnTime.toTimestamp(),
            startTime.toTimestamp(),
            endTime.toTimestamp(),
            businessId
        )

    override fun listTransactionsSortedByBillDate(
        customerId: String,
        startTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> =
        localSource.get().listTransactionsSortedByBillDate(customerId, startTime.toTimestamp(), businessId)

    override fun listTransactions(
        customerId: String,
        startTime: Long,
        businessId: String,
    ): Observable<List<Transaction>> =
        localSource.get().listTransactions(customerId, startTime.toTimestamp(), businessId)

    override fun listTransactions(customerId: String, businessId: String): Observable<List<Transaction>> =
        localSource.get().listTransactions(customerId, businessId)

    override fun listNonDeletedTransactionsByBillDate(customerId: String, businessId: String): Observable<List<Transaction>> =
        localSource.get().listNonDeletedTransactionsByBillDate(customerId, businessId)

    override fun listDirtyTransactions(isDirty: Boolean, businessId: String): Observable<List<Transaction>> =
        localSource.get().listDirtyTransactions(isDirty, businessId)

    override fun deleteLocalTransactionsForCustomer(accountId: String) =
        localSource.get().deleteLocalTransactionsForCustomer(accountId)

    override fun lastUpdatedTransactionTime(businessId: String): Single<Timestamp> =
        localSource.get().lastUpdatedTransactionTime(businessId)

    override fun getTxnAmountHistory(transactionId: String, businessId: String): Single<TransactionAmountHistory> =
        remoteSource.get().getTxnAmountHistory(transactionId, businessId)

    override fun getTransactionsIdsByCreatedTime(
        startTime: Long,
        endTime: Long,
        businessId: String,
    ): Single<List<String>> =
        localSource.get().getTransactionsIdsByCreatedTime(startTime.toTimestamp(), endTime.toTimestamp(), businessId)

    override fun markTransactionsDirtyAndCreateCommands(transactionIds: List<String>, businessId: String): Completable {
        tracker.get().trackDebug(
            TRANSACTION_RECOVERY,
            "$TAG count: ${transactionIds.size}, ids: $transactionIds"
        )
        val completableList = transactionIds.map { transactionId ->
            localSource.get().isTransactionPresent(transactionId)
                .flatMapCompletable { isPresent ->
                    if (isPresent) {
                        localSource.get().getTransaction(transactionId).firstOrError()
                            .map { buildCreateTransactionCommandForTransaction(it) }
                            .flatMapCompletable { command ->
                                localSource.get()
                                    .markTransactionDirtyAndInsertCommandIfNotPresent(
                                        transactionId,
                                        command,
                                        businessId
                                    )
                            }
                    } else {
                        tracker.get().trackDebug(
                            TRANSACTION_NOT_FOUND,
                            "$TAG getTransaction markTransactionsDirtyAndCreateCommands: [$transactionId]"
                        )
                        Completable.complete()
                    }
                }
        }
        return Completable.concat(completableList)
    }

    private fun buildCreateTransactionCommandForTransaction(transaction: Transaction): Command.CreateTransaction {
        val command = Command.CreateTransaction(
            customerId = transaction.customerId,
            transactionId = transaction.id,
            type = transaction.type,
            amount = transaction.amount,
            note = transaction.note,
            billDate = transaction.billDate,
            inputType = transaction.inputType,
            voiceId = transaction.voiceId
        )
        command.transactionImages = transaction.images
        command.timestamp = transaction.createdAt
        return command
    }

    override fun bulkSearchTransactions(
        actionId: String,
        transactionIds: List<String>,
        businessId: String,
    ): Single<List<String>> =
        remoteSource.get().bulkSearchTransactions(actionId, transactionIds, businessId).map { it.missingTransactionIds }

    override fun getNumberOfSyncTransactionsTillGivenUpdatedTime(
        maximumUpdateTime: Long,
        businessId: String,
    ): Single<Int> {
        return localSource.get().getTransactionsCountTillGivenUpdatedTime(Timestamp(maximumUpdateTime), businessId)
    }

    // ================================================================================
    // Customer
    // ================================================================================

    override fun createCustomer(
        description: String,
        mobile: String,
        profileImage: String?,
        businessId: String,
    ): Single<Customer> {
        var profileImageUploadTask = Completable.complete()
        profileImage?.let {
            val imageFile = File(profileImage)
            if (imageFile.exists()) {
                val url = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + CoreUtils.generateRandomId() + ".jpg"
                profileImageUploadTask = uploadFile.get().schedule(CONTACT_PHOTO, url, profileImage)
            } else {
                tracker.get().trackDebug(CoreTracker.DebugType.FILE_NOT_FOUND, "createCustomer filePath: $profileImage")
            }
        }
//        if(experiment) {
//            if(disable_offline_add_customer) {
//                add customer online only
//            } else {
//                offline sync
//            }
//        } else {
//            add customer online only
//        }

        return offlineAddCustomerAbHelper.get().isEligibleForOfflineAddCustomer().flatMap { experiment ->
            if (experiment) {
                offlineAddCustomerAbHelper.get().isDisableOfflineAddCustomerFeature().flatMap { feature ->
                    if (feature) {
                        addCustomerOnlineOnly(description, mobile, profileImage, businessId)
                    } else {
                        addCustomerOfflineOnly(description, mobile, profileImage, businessId)
                    }
                }
            } else {
                addCustomerOnlineOnly(description, mobile, profileImage, businessId)
            }
        }.flatMap { customer ->
            profileImageUploadTask.toSingleDefault(customer)
        }
    }

    private fun addCustomerOfflineOnly(
        description: String,
        mobile: String?,
        profileImage: String?,
        businessId: String,
    ): Single<Customer> {
        return rxSingle {
            localSource.get().createOfflineCustomer(
                description = description,
                mobile = mobile,
                profileImage = profileImage,
                businessId = businessId
            )
        }.flatMap {
            syncCustomers.get().schedule(businessId).toSingleDefault(it)
        }
    }

    private fun addCustomerOnlineOnly(
        description: String,
        mobile: String?,
        profileImage: String?,
        businessId: String,
    ): Single<Customer> {
        return remoteSource.get().addCustomer(
            description = description,
            mobile = mobile,
            profileImage = profileImage,
            businessId = businessId
        )
            .flatMap { customer ->
                localSource.get().putCustomer(customer, businessId)
                    .toSingleDefault(customer)
            }
    }

    override suspend fun getCustomersByMobile(mobile: String, businessId: String): List<Customer> {
        return localSource.get().getCustomersByMobile(mobile, businessId)
    }

    override suspend fun deleteImmutableAccount(customerId: String) {
        return localSource.get().deleteImmutableAccount(customerId)
    }

    override fun getUnSyncedCustomerCount(businessId: String): Flow<Int> {
        return localSource.get().getCommandsCount(
            listOf(
                CREATE_CUSTOMER_DIRTY,
                CREATE_CUSTOMER_IMMUTABLE,
            ),
            businessId
        )
    }

    override fun getImmutableCustomersCount(businessId: String): Flow<Int> {
        return localSource.get().getCommandsCount(
            listOf(CREATE_CUSTOMER_IMMUTABLE),
            businessId
        )
    }

    override suspend fun getIsBlocked(businessId: String, customerId: String): Boolean {
        return localSource.get().getIsBlocked(businessId, customerId)
    }

    override suspend fun getIsAddTransactionRestricted(businessId: String, customerId: String): Boolean {
        return localSource.get().getIsAddTransactionRestricted(businessId, customerId)
    }

    override fun deleteCustomer(customerId: String, businessId: String): Completable {
        return syncCustomer.get().execute(customerId, businessId)
            .andThen(remoteSource.get().deleteCustomer(customerId, businessId))
            .andThen(remoteSource.get().getCustomer(customerId, businessId))
            .flatMapCompletable { localSource.get().putCustomer(it, businessId) }
    }

    override fun updateCustomer(
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
    ): Completable {
        return remoteSource.get().updateCustomer(
            customerId,
            desc,
            address,
            profileImage,
            mobile,
            lang,
            reminderMode,
            txnAlertEnabled,
            isForTxnEnable,
            dueInfoActiveDate,
            updateDueCustomDate,
            deleteDueCustomDate,
            addTransactionPermission,
            updateAddTransactionRestricted,
            blockTransaction,
            updateBlockTransaction,
            businessId
        )
            .flatMapCompletable { localSource.get().putCustomer(it, businessId) }
    }

    override fun getCustomer(customerId: String): Observable<Customer> = localSource.get().getCustomer(customerId)

    override fun getCustomerByMobile(mobile: String, businessId: String): Single<Customer> =
        localSource.get().getCustomerByMobile(mobile, businessId)

    override fun listCustomers(businessId: String): Observable<List<Customer>> =
        localSource.get().listCustomers(businessId)

    override fun listCustomersByLastPayment(businessId: String): Observable<List<Customer>> =
        localSource.get().listCustomersByLastPayment(businessId)

    override fun listActiveCustomers(businessId: String): Observable<List<Customer>> =
        localSource.get().listActiveCustomers(businessId)

    override fun listActiveCustomersIds(businessId: String): Observable<List<String>> =
        localSource.get().listActiveCustomersIds(businessId)

    override fun getCustomerCount(businessId: String): Observable<Int> = localSource.get().getCustomerCount(businessId)

    override fun getActiveCustomerCount(businessId: String): Observable<Long> =
        localSource.get().getActiveCustomerCount(businessId)

    override fun markActivityAsSeen(customerId: String): Completable = localSource.get().markActivityAsSeen(customerId)

    override fun syncCustomer(customerId: String, businessId: String?): Completable {
        return syncCustomer.get().execute(customerId, businessId)
    }

    override fun scheduleSyncCustomer(customerId: String, businessId: String): Completable {
        return syncCustomer.get().schedule(customerId, businessId)
    }

    override fun syncCustomers(businessId: String?): Completable {
        return syncCustomers.get().execute(businessId = businessId)
    }

    override fun scheduleSyncCustomers(businessId: String): Completable {
        return syncCustomers.get().schedule(businessId)
    }

    override fun reactivateCustomer(
        name: String?,
        customerId: String,
        localProfileImage: String?,
        businessId: String,
    ): Single<Customer> {
        var profileImageUploadTask = Completable.complete()
        var profileImageUrl: String? = null
        localProfileImage?.let {
            val imageFile = File(localProfileImage)
            if (imageFile.exists()) {
                val url = IUploadFile.AWS_RECEIPT_BASE_URL + "/" + CoreUtils.generateRandomId() + ".jpg"
                profileImageUploadTask = uploadFile.get().schedule(CUSTOMER_PHOTO, url, localProfileImage)
                profileImageUrl = url
            } else {
                tracker.get().trackDebug(
                    CoreTracker.DebugType.FILE_NOT_FOUND,
                    "reactivateCustomer filePath: $localProfileImage"
                )
            }
        }
        return CoreUtils.validateName(name)
            .andThen(localSource.get().getCustomer(customerId))
            .firstOrError()
            .flatMap {
                val desc = if (name.isNullOrEmpty().not()) name!! else it.description
                remoteSource.get().addCustomer(desc, it.mobile, true, profileImageUrl, null, businessId)
            }
            .flatMap {
                localSource.get().putCustomer(it, businessId)
                    .andThen(profileImageUploadTask)
                    .andThen(Single.just(it))
            }
    }

    override fun deleteLocalCustomer(accountId: String) = localSource.get().deleteCustomer(accountId)

    override fun updateLocalCustomerDescription(description: String, customerId: String) =
        localSource.get().updateCustomerDescription(description, customerId)

    override fun getFirstTransaction(businessId: String) = localSource.get().getFirstTransaction(businessId)

    override fun getLastTransaction(businessId: String) = localSource.get().getLastTransaction(businessId)

    override fun getLatestTransactionAddedByCustomer(customerId: String, businessId: String) =
        localSource.get().getLatestTransactionAddedByCustomer(customerId, businessId)

    override fun getLatestTransaction(customerId: String) =
        localSource.get().getLatestTransaction(customerId)

    override fun quickAddTransaction(
        customer: QuickAddCustomerModel,
        amount: Long,
        type: Transaction.Type,
        profileImageUploadUrl: String?,
        businessId: String,
    ): Single<QuickAddTransactionResponse> {
        val quickAddCustomerRequestModel =
            QuickAddCustomerRequestModel(customer.name, customer.mobile, profileImageUploadUrl)
        val transaction = QuickAddTransactionModel(amount, type.code)
        val request = QuickAddTransactionRequest(businessId, quickAddCustomerRequestModel, transaction)
        return remoteSource.get().quickAddTransaction(request, businessId).subscribeOn(schedulerProvider.get().io())
    }

    override fun putCustomer(customer: Customer, businessId: String) =
        localSource.get().putCustomer(customer, businessId)

    override fun putTransaction(transaction: Transaction, businessId: String) =
        localSource.get().putTransaction(transaction, businessId)

    override fun coreUpdateCustomerAddTransactionPermission(accountID: String, isDenied: Boolean) =
        localSource.get().updateCustomerAddTransactionPermission(accountID, isDenied)

    override fun getDefaulters(businessId: String) = localSource.get().getDefaulters(businessId)

    override fun getCustomersWithBalanceDue(businessId: String) =
        localSource.get().getCustomersWithBalanceDue(businessId)

    override fun listOnlineTransactions(customerId: String): Observable<List<Transaction>> {
        return localSource.get().listOnlineTransactions(customerId)
    }

    override fun getLiveSalesCustomerId(businessId: String): Single<String> {
        return localSource.get().getLiveSalesCustomerId(businessId)
    }

    override fun getSuggestionsFromStore(businessId: String) =
        localSource.get().getSuggestedCustomerIdsForAddTransaction(businessId)

    override fun getSuggestionsFromServer(businessId: String) =
        remoteSource.get().getSuggestedCustomerIdsForAddTransaction(businessId)

    override fun replaceSuggestedCustomerIdsForAddTransaction(ids: List<String>, businessId: String) =
        localSource.get().replaceSuggestedCustomerIdsForAddTransaction(ids, businessId)

    override fun getTransactionIdForCollection(collectionId: String, businessId: String) =
        localSource.get().getTransactionIdForCollection(collectionId, businessId)

    private fun scheduleSyncTransaction(source: String, businessId: String): Completable {
        return syncTransactions.get().schedule(source, businessId)
    }

    override fun getDefaultersDataForBanner(defaulterSince: String, businessId: String): Flow<BulkReminderDbInfo> {
        return localSource.get().getDefaultersDataForBanner(defaulterSince, businessId)
    }

    override fun getDefaultersForPendingReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<CoreDbReminderProfile>> {
        return localSource.get().getDefaultersForPendingReminders(defaulterSince, businessId)
    }

    override fun getDefaultersForTodaysReminders(
        defaulterSince: String,
        businessId: String,
    ): Flow<List<CoreDbReminderProfile>> {
        return localSource.get().getDefaultersForTodaysReminders(defaulterSince, businessId)
    }

    override suspend fun updateLastReminderSendTime(
        customerId: String,
        lastReminderSentTime: Timestamp,
        businessId: String,
    ) {
        localSource.get().updateLastReminderSendTime(customerId, lastReminderSentTime, businessId)
    }

    override suspend fun getDirtyLastReminderSendTime(
        customerIds: List<String>,
        businessId: String,
    ): List<CoreLastReminderSendTime> {
        return localSource.get().getDirtyLastReminderSendTime(customerIds, businessId)
    }

    override suspend fun setCustomersLastReminderSendTimeToServer(
        lastReminderSendTimeList: List<LastReminderSendTime>,
        businessId: String,
    ) {
        withContext(Dispatchers.IO) {
            val request = SetRemindersApiRequest(reminders = lastReminderSendTimeList)
            remoteSource.get().setCustomersLastReminderSendTime(request, businessId)
        }
    }
}
