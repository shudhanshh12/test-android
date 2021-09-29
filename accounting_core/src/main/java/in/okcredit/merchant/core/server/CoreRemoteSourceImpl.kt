package `in`.okcredit.merchant.core.server

import `in`.okcredit.merchant.core.*
import `in`.okcredit.merchant.core.common.CoreException
import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.common.toTimestamp
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.model.Customer.*
import `in`.okcredit.merchant.core.model.History
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import `in`.okcredit.merchant.core.model.TransactionImage
import `in`.okcredit.merchant.core.model.bulk_reminder.SetRemindersApiRequest
import `in`.okcredit.merchant.core.server.internal.CoreApiClient
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages.*
import `in`.okcredit.merchant.core.server.internal.bulk_search_transactions.BulkSearchTransactionsRequest
import `in`.okcredit.merchant.core.server.internal.bulk_search_transactions.BulkSearchTransactionsResponse
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionRequest
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.error.Error
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import javax.inject.Inject

class CoreRemoteSourceImpl @Inject constructor(
    private val apiClient: Lazy<CoreApiClient>,
) : CoreRemoteSource {

    override suspend fun pushCustomerCommands(
        request: PushCustomersCommandsRequest,
        businessId: String,
    ): PushCustomersCommandsResponse {
        return apiClient.get().pushCustomerCommands(request, businessId)
    }

    override fun pushTransactionCommands(
        requestTransactions: PushTransactionsCommandsRequest,
        businessId: String,
    ): Single<PushTransactionsCommandsResponse> {
        return apiClient.get().pushTransactionCommands(requestTransactions, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun getTransactions(
        startDate: Long,
        source: String,
        businessId: String,
    ): Single<CoreApiMessages.GetTransactionsResponse> {
        return apiClient.get().getTransactions(
            CoreApiMessages.GetTransactionsRequest(
                CoreApiMessages.TransactionsRequest(type = 0, role = 1, start_time_ms = startDate)
            ),
            source = "core_module_$source",
            businessId = businessId
        )
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }

    override fun getTxnAmountHistory(transactionId: String, businessId: String): Single<TransactionAmountHistory> {
        return apiClient.get().getTxnAmountHistory(
            GetTransactionAmountHistoryRequest(
                transaction_id = transactionId
            ),
            businessId
        ).subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }.map {
                it.toTransactionAmountHistory()
            }
    }

    override fun getTransactionFile(id: String, businessId: String): Single<GetTransactionFileResponse> {
        return apiClient.get().getTransactionFile(GetTransactionFileRequest(id), businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }
    }

    override fun getTransaction(transactionId: String, businessId: String): Single<Transaction> {
        return apiClient.get().getTransaction(transactionId, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }.map { it.toTransaction() }
    }

    override fun bulkSearchTransactions(
        actionId: String,
        transactionIds: List<String>,
        businessId: String,
    ): Single<BulkSearchTransactionsResponse> {
        return apiClient.get()
            .bulkSearchTransactions(BulkSearchTransactionsRequest(actionId, transactionIds), businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }
    }

    override fun addCustomer(
        description: String,
        mobile: String?,
        reactivate: Boolean,
        profileImage: String?,
        email: String?,
        businessId: String
    ): Single<Customer> {
        return apiClient.get().addCustomer(
            CoreApiMessages.AddCustomerRequest(
                mobile,
                description,
                reactivate,
                profileImage
            ),
            businessId
        )
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .flatMap { response ->
                if (response.isSuccessful) {
                    Single.just(response.body())
                        .map { it.toCustomer() }
                } else {
                    val error = Error.parse(response)
                    if (error.code == 409 && "customer_exists" == error.message) {
                        // mobile already registered with another customer, find that customer and throw an error
                        apiClient.get().listCustomers(mobile, true, businessId)
                            .subscribeOn(ThreadUtils.api())
                            .observeOn(ThreadUtils.worker())
                            .map { findByMobileRes ->
                                if (findByMobileRes.isSuccessful) {
                                    val customers: List<CoreApiMessages.ApiCustomer>? = findByMobileRes.body()
                                    if (customers?.size != 1) {
                                        throw IllegalAccessException("mobile conflict but find_by_mobile not valid")
                                    }
                                    customers[0]
                                } else {
                                    throw Error.parse(findByMobileRes)
                                }
                            }
                            .map { it.toCustomer() }
                            .flatMap { customer ->
                                if (customer.status == 1) {
                                    Single.error(CoreException.MobileConflict(customer))
                                } else {
                                    Single.error(CoreException.DeletedCustomer(customer))
                                }
                            }
                    } else {
                        Single.error(error)
                    }
                }
            }
    }

    override fun getCustomer(customerId: String, businessId: String): Single<Customer> {
        return apiClient.get().getCustomer(customerId, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }.map { it.toCustomer() }
    }

    override fun listCustomers(mobile: String?, businessId: String): Single<List<Customer>> {
        return apiClient.get().listCustomers(mobile, true, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful) {
                    return@map res.body()
                } else {
                    throw Error.parse(res)
                }
            }.map {
                it.toCustomerList()
            }
    }

    override fun deleteCustomer(customerId: String, businessId: String): Completable {
        return apiClient.get().deleteCustomer(customerId, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .flatMapCompletable { res ->
                if (res.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    val error = Error.parse(res)
                    if (error.code == 409) {
                        return@flatMapCompletable Completable.error(CoreException.DeletePermissionDenied(error.error))
                    } else {
                        return@flatMapCompletable Completable.error(error)
                    }
                }
            }
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
        businessId: String
    ): Single<Customer> {
        val updateCustomerRequest = CoreApiMessages.UpdateCustomerRequest(
            mobile = mobile,
            description = desc,
            address = address,
            profile_image = profileImage,
            lang = lang,
            reminder_mode = reminderMode,
            txn_alert_enabled = txnAlertEnabled,
            update_txn_alert_enabled = isForTxnEnable,
            due_custom_date = dueInfoActiveDate?.seconds,
            update_due_custom_date = updateDueCustomDate,
            delete_due_custom_date = deleteDueCustomDate,
            update_add_transaction_restricted = updateAddTransactionRestricted,
            add_transaction_restricted = addTransactionPermission,
            state = blockTransaction,
            update_state = updateBlockTransaction
        )

        return apiClient.get().updateCustomer(customerId, updateCustomerRequest, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .flatMap { res ->
                if (res.isSuccessful) {
                    Single.just(res.body()).map { it.toCustomer() }
                } else {
                    val error = Error.parse(res)
                    if (error.code == 403) {
                        throw CoreException.MobileUpdateAccessDenied
                    } else if (error.code == 409 && "customer_exists" == error.message) {
                        // mobile already registered with another customer
                        // find that customer and throw an error
                        apiClient.get().listCustomers(mobile, true, businessId)
                            .subscribeOn(ThreadUtils.api())
                            .observeOn(ThreadUtils.worker())
                            .map { findByMobileRes ->
                                if (findByMobileRes.isSuccessful) {
                                    val customers: List<CoreApiMessages.ApiCustomer?> = findByMobileRes
                                        .body()!!
                                    if (customers.size != 1) {
                                        throw IllegalAccessException("mobile conflict but find_by_mobile not valid")
                                    }
                                    return@map customers[0]
                                } else {
                                    throw Error.parse(findByMobileRes)
                                }
                            }
                            .map { it.toCustomer() }
                            .flatMap { customer -> Single.error<Customer>(CoreException.MobileConflict(customer)) }
                    } else {
                        return@flatMap Single.error<Customer>(error)
                    }
                }
            }
    }

    override fun quickAddTransaction(request: QuickAddTransactionRequest, businessId: String) = apiClient.get().quickAddTransaction(request, businessId)

    override fun getSuggestedCustomerIdsForAddTransaction(businessId: String): Single<List<String>> {
        return apiClient.get().getSuggestedCustomerIdsForAddTransaction(businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map { res ->
                if (res.isSuccessful && res.body() != null) {
                    return@map res.body()
                } else {
                    throw res.asError()
                }
            }.map { it.accountIds }
    }

    override suspend fun setCustomersLastReminderSendTime(request: SetRemindersApiRequest, businessId: String) {
        return apiClient.get().setCustomersLastReminderSendTime(request, businessId)
    }
}

fun Command.toApiCustomerCommand(customer: Customer): ApiCustomerCommand {
    return when (this) {
        is Command.CreateCustomerDirty,
        is Command.CreateCustomerImmutable,
        -> ApiCustomerCommand(
            id = this.id,
            timestamp = this.timestamp.epoch,
            action = CustomerAction.ADD.value,
            customer = customer.toApiSyncCustomer()
        )

        else -> throw CoreException.IllegalArgumentException
    }
}

private fun Customer.toApiSyncCustomer(): ApiSyncCustomer = ApiSyncCustomer(
    id = this.id,
    status = this.status,
    mobile = this.mobile,
    description = this.description,
    created_at = this.createdAt.epoch,
    profile_image = this.profileImage,
    address = this.address,
    email = this.email,
    lang = this.lang,
    blocked_by_customer = this.blockedByCustomer,
)

fun List<Command>.toApiTransactionCommandList(): List<ApiTransactionCommand> {
    return this.map { it.toApiTransactionCommand() }
}

private fun Command.toApiTransactionCommand(): ApiTransactionCommand {
    val apiCommandType = this.commandType.apiCommandType
    return when (this) {
        is Command.CreateTransaction, is Command.UpdateTransactionNote, is Command.DeleteTransaction,
        is Command.UpdateTransactionAmount,
        -> ApiTransactionCommand(
            id = this.id,
            transaction_id = this.transactionId,
            type = apiCommandType.type.value,
            path = apiCommandType.path,
            transaction = getTransactionValue(this),
            timestamp = this.timestamp.epoch,
            mask = apiCommandType.mask
        )
        is Command.CreateTransactionImage -> ApiTransactionCommand(
            id = this.id,
            transaction_id = this.transactionId,
            type = apiCommandType.type.value,
            path = apiCommandType.path,
            image_id = this.imageId,
            image = TransactionImage(url = this.url),
            timestamp = this.timestamp.epoch
        )
        is Command.DeleteTransactionImage -> ApiTransactionCommand(
            id = this.id,
            transaction_id = this.transactionId,
            type = apiCommandType.type.value,
            path = apiCommandType.path,
            image_id = this.imageId,
            timestamp = this.timestamp.epoch
        )
        else -> throw CoreException.IllegalArgumentException
    }
}

private fun getTransactionValue(command: Command): CoreApiMessages.Transaction {
    return when (command) {
        is Command.CreateTransaction -> Transaction(
            id = command.transactionId,
            account_id = command.customerId,
            type = command.type.code,
            amount = command.amount,
            creator_role = CoreApiMessages.ROLE_SELLER,
            create_time_ms = command.timestamp.epoch,
            note = command.note,
            images = command.transactionImages?.map {
                TransactionImage(
                    id = it.id, transaction_id = command.transactionId, url = it.url, create_time = it.createdAt.epoch
                )
            },
            bill_date_ms = command.billDate?.epoch ?: command.timestamp.epoch,
            alert_sent_by_creator = false,
            meta = Meta(command.inputType, command.voiceId),
            transaction_state = Transaction.State.CREATED.code
        )
        is Command.UpdateTransactionNote -> Transaction(
            note = command.note
        )
        is Command.DeleteTransaction -> Transaction(
            transaction_state = Transaction.State.DELETED.code,
            deleter_role = CoreApiMessages.ROLE_SELLER
        )
        is Command.UpdateTransactionAmount -> Transaction(
            amount = command.amount
        )
        else -> throw CoreException.IllegalArgumentException
    }
}

fun CoreApiMessages.Transaction.toTransaction() = Transaction(
    id = this.id!!,
    type = Transaction.Type.getTransactionType(this.type!!),
    customerId = this.account_id!!,
    amount = this.amount!!,
    collectionId = this.collection_id,
    images = this.images.toTransactionImageList(),
    note = this.note,
    createdAt = this.create_time_ms!!.toTimestamp(),
    isDeleted = this.deleted!!,
    deleteTime = this.delete_time_ms?.toTimestamp(),
    isDirty = false,
    billDate = this.bill_date_ms!!.toTimestamp(),
    updatedAt = this.update_time_ms!!.toTimestamp(),
    smsSent = this.alert_sent_by_creator!!,
    createdByCustomer = this.creator_role == CoreApiMessages.ROLE_BUYER,
    deletedByCustomer = this.deleter_role == CoreApiMessages.ROLE_BUYER,
    state = Transaction.State.getTransactionState(this.transaction_state!!),
    category = Transaction.Category.getTransactionCategory(this.tx_category!!),
    amountUpdated = this.amount_updated ?: false,
    amountUpdatedAt = this.amount_updated_at?.toTimestamp()
)

private fun List<CoreApiMessages.TransactionImage>?.toTransactionImageList(): List<TransactionImage> =
    this?.map { it.toTransactionImage() } ?: listOf()

private fun CoreApiMessages.TransactionImage.toTransactionImage() =
    TransactionImage(
        id = this.id!!,
        url = this.url,
        transactionId = this.transaction_id!!,
        createdAt = this.create_time!!.toTimestamp()
    )

internal enum class ApiCommandType(val type: Type, val path: String, val mask: List<String>?) {
    UNKNOWN(Type.UNKNOWN, "", null),
    CREATE_TRANSACTION(Type.ADD, Path.TRANSACTION.value, null),
    UPDATE_TRANSACTION_NOTE(Type.UPDATE, Path.TRANSACTION.value, listOf("note")),
    UPDATE_TRANSACTION_AMOUNT(Type.UPDATE, Path.TRANSACTION.value, listOf("amount")),
    DELETE_TRANSACTION(Type.UPDATE, Path.TRANSACTION.value, listOf("transaction_state", "deleter_role")),
    CREATE_TRANSACTION_IMAGE(Type.ADD, Path.IMAGES.value, null),
    DELETE_TRANSACTION_IMAGE(Type.DELETE, Path.IMAGES.value, null);

    internal enum class Type(val value: Int) {
        UNKNOWN(0), ADD(1), UPDATE(2), DELETE(3)
    }

    internal enum class Path(val value: String) {
        TRANSACTION("/transactions"), IMAGES("/images")
    }
}

private val Command.CommandType.apiCommandType: ApiCommandType
    get() = when (this) {
        Command.CommandType.CREATE_TRANSACTION -> ApiCommandType.CREATE_TRANSACTION
        Command.CommandType.UPDATE_TRANSACTION_NOTE -> ApiCommandType.UPDATE_TRANSACTION_NOTE
        Command.CommandType.UPDATE_TRANSACTION_AMOUNT -> ApiCommandType.UPDATE_TRANSACTION_AMOUNT
        Command.CommandType.DELETE_TRANSACTION -> ApiCommandType.DELETE_TRANSACTION
        Command.CommandType.CREATE_TRANSACTION_IMAGE -> ApiCommandType.CREATE_TRANSACTION_IMAGE
        Command.CommandType.DELETE_TRANSACTION_IMAGE -> ApiCommandType.DELETE_TRANSACTION_IMAGE
        else -> ApiCommandType.UNKNOWN
    }

fun Int.toStatus(): Status {
    return when (this) {
        Status.FAILURE.value -> Status.FAILURE
        Status.SUCCESS.value -> Status.SUCCESS
        else -> Status.FAILURE
    }
}

fun ApiCustomer.toCustomer(): Customer {
    return Customer(
        id = this.id,
        customerSyncStatus = CustomerSyncStatus.CLEAN.code, // directly from backend so keeping it as Clean
        status = this.status,
        mobile = this.mobile,
        description = this.description,
        createdAt = this.created_at.toTimestamp(),
        txnStartTime = this.txn_start_time?.toTimestamp(),
        accountUrl = this.account_url,
        balance = this.balance_v2,
        transactionCount = this.tx_count,
        lastActivity = this.last_activity?.toTimestamp(),
        lastPayment = this.last_payment?.toTimestamp(),
        profileImage = this.profile_image,
        address = this.address,
        email = this.email,
        newActivityCount = 0,
        addTransactionPermissionDenied = this.add_transaction_restricted,
        registered = this.registered,
        lastBillDate = null,
        txnAlertEnabled = this.txn_alert_enabled,
        lang = this.lang,
        reminderMode = this.reminder_mode,
        isLiveSales = this.is_live_sales,
        lastActivityMetaInfo = null,
        lastAmount = null,
        lastViewTime = null,
        blockedByCustomer = this.blocked_by_customer,
        state = State.getState(this.state),
        restrictContactSync = this.restrict_contact_sync,
        lastReminderSendTime = this.last_reminder_sent?.toTimestamp() ?: Timestamp(0)
    )
}

internal fun List<ApiCustomer>.toCustomerList(): List<Customer> {
    return this.map { it.toCustomer() }
}

internal fun GetTransactionAmountHistoryResponse.toTransactionAmountHistory(): TransactionAmountHistory {
    return TransactionAmountHistory(
        transactionId = this.transaction.transactionId,
        amount = this.transaction.amount,
        amountUpdated = this.transaction.amountUpdated,
        amountUpdatedAt = this.transaction.amountUpdatedAt?.toTimestamp(),
        initialTransactionAmount = this.transaction.initial?.amount?.toLong(),
        initialTransactionCreatedAt = this.transaction.initial?.createdAt?.toTimestamp(),
        history = this.transaction.history.toTransactionHistoryList()
    )
}

private fun List<CoreApiMessages.History>?.toTransactionHistoryList(): List<History> =
    this?.map { it.toTransactionHistory() } ?: listOf()

private fun CoreApiMessages.History.toTransactionHistory() =
    History(
        oldAmount = this.oldAmount?.toLong(),
        newAmount = this.newAmount?.toLong(),
        createdAt = this.createdAt!!.toTimestamp()
    )
