package `in`.okcredit.merchant.core.server

import `in`.okcredit.merchant.core.common.Timestamp
import `in`.okcredit.merchant.core.model.Customer
import `in`.okcredit.merchant.core.model.Transaction
import `in`.okcredit.merchant.core.model.TransactionAmountHistory
import `in`.okcredit.merchant.core.model.bulk_reminder.SetRemindersApiRequest
import `in`.okcredit.merchant.core.server.internal.CoreApiMessages
import `in`.okcredit.merchant.core.server.internal.bulk_search_transactions.BulkSearchTransactionsResponse
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionRequest
import `in`.okcredit.merchant.core.server.internal.quick_add_transaction.QuickAddTransactionResponse
import io.reactivex.Completable
import io.reactivex.Single

interface CoreRemoteSource {

    fun pushTransactionCommands(
        requestTransactions: CoreApiMessages.PushTransactionsCommandsRequest,
        businessId: String,
    ): Single<CoreApiMessages.PushTransactionsCommandsResponse>

    fun getTransactions(
        startDate: Long,
        source: String,
        businessId: String,
    ): Single<CoreApiMessages.GetTransactionsResponse>

    fun getTxnAmountHistory(transactionId: String, businessId: String): Single<TransactionAmountHistory>

    fun getTransactionFile(id: String, businessId: String): Single<CoreApiMessages.GetTransactionFileResponse>

    fun getTransaction(transactionId: String, businessId: String): Single<Transaction>

    fun bulkSearchTransactions(
        actionId: String,
        transactionIds: List<String>,
        businessId: String,
    ): Single<BulkSearchTransactionsResponse>

    suspend fun pushCustomerCommands(
        request: CoreApiMessages.PushCustomersCommandsRequest,
        businessId: String,
    ): CoreApiMessages.PushCustomersCommandsResponse

    fun addCustomer(
        description: String,
        mobile: String?,
        reactivate: Boolean = false,
        profileImage: String?,
        email: String? = null,
        businessId: String,
    ): Single<Customer>

    fun getCustomer(customerId: String, businessId: String): Single<Customer>

    fun listCustomers(mobile: String?, businessId: String): Single<List<Customer>>

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
    ): Single<Customer>

    fun quickAddTransaction(
        request: QuickAddTransactionRequest,
        businessId: String,
    ): Single<QuickAddTransactionResponse>

    fun getSuggestedCustomerIdsForAddTransaction(businessId: String): Single<List<String>>

    suspend fun setCustomersLastReminderSendTime(request: SetRemindersApiRequest, businessId: String)
}
