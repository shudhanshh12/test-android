package `in`.okcredit.merchant.core.server.internal

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface CoreApiMessages {

    companion object {
        const val TYPE_LIST = 0
        const val TYPE_FILE = 1

        const val ROLE_UNKNOWN = 0
        const val ROLE_SELLER = 1 // Suppliers
        const val ROLE_BUYER = 2 // Customers

        const val FILE_CREATION_IN_PROGRESS = 0
        const val FILE_CREATION_COMPLETED = 1
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class Transaction(
        val id: String? = null,
        val account_id: String? = null,
        val type: Int? = null,
        val amount: Long? = null,
        val creator_role: Int? = null,
        val create_time: Long? = null,
        val deleted: Boolean? = null,
        val deleter_role: Int? = null,
        val delete_time: Long? = null,
        val note: String? = null,
        val images: List<TransactionImage>? = null,
        val bill_date: Long? = null,
        val alert_sent_by_creator: Boolean? = null,
        val collection_id: String? = null,
        val update_time: Long? = null,
        val receipt_url: String? = null,
        val meta: Meta? = null,
        val transaction_state: Int? = null,
        val tx_category: Int? = null,
        val create_time_ms: Long? = null,
        val delete_time_ms: Long? = null,
        val bill_date_ms: Long? = null,
        val update_time_ms: Long? = null,
        val amount_updated: Boolean? = null,
        val amount_updated_at: Long? = null,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class TransactionImage(
        var id: String? = null,
        val transaction_id: String? = null,
        var url: String,
        var create_time: Long? = null,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class GetTransactionsRequest(
        val txn_req: TransactionsRequest,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class TransactionsRequest(
        val type: Int, // type of transactions (MERCHANT = 0, ACCOUNT = 1)
        val account_id: String? = null, // filter to get specific account transactions.
        val role: Int? = null, // denotes role of a merchant in an account (UNKNOWN = 0, SELLER = 1, BUYER = 2)
        val start_time: Long? = null,
        val end_time: Long? = null,
        val exclude_deleted: Long? = null,
        val order_by: Int? = 1, // denotes ordering of transactions (UNKNOWN = 0, UPDATE_TIME = 1, BILL_DATE = 2)
        val start_time_ms: Long? = null,
        val end_time_ms: Long? = null,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class GetTransactionsResponse(
        val type: Int, // type of transactions (LIST = 0, FILE = 1)
        val list_data: ListData?, // transactions info if type is list.
        val file_data: FileData?, // denotes role of a merchant in an account (UNKNOWN = 0, SELLER = 1, BUYER = 2)
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class ListData(
        val transactions: List<Transaction>,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class FileData(
        val txn_file: TransactionFile,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class TransactionFile(
        val id: String,
        val status: Int?, // denotes role of a merchant in an account (FILE_DOWNLOAD_IN_PROGRESS = 0, FILE_DOWNLOAD_COMPLETED = 1)
        val encryption_key: String?,
        val file: String?,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class GetTransactionFileRequest(
        val id: String,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class GetTransactionFileResponse(
        val txn_file: TransactionFile,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class ApiTransactionCommand(
        val id: String,
        val type: Int,
        val path: String,
        val transaction: Transaction? = null,
        val image: TransactionImage? = null,
        val timestamp: Long,
        val mask: List<String>? = null,
        val transaction_id: String,
        val image_id: String? = null,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class Meta(val intent: String? = null, val intent_id: String? = null)

    // --------------------------

    @JsonClass(generateAdapter = true)
    data class PushCustomersCommandsRequest(val operations: List<ApiCustomerCommand>)

    @Keep
    @JsonClass(generateAdapter = true)
    data class PushCustomersCommandsResponse(val operation_responses: List<CustomerOperationResponse>)

    @Keep
    @JsonClass(generateAdapter = true)
    data class CustomerOperationResponse(
        val id: String, // commandId
        val status: Int,
        val error: Error?,
        val customer: ApiCustomer? = null,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class ApiCustomerCommand(
        val id: String,
        val action: String,
        val timestamp: Long,
        val customer: ApiSyncCustomer,
    )

    enum class CustomerAction(val value: String) {
        ADD("ADD"),
        EDIT("EDIT"), // unsupported for now
        DELETE("DELETE") // unsupported for now
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class ApiSyncCustomer(
        val id: String,
        val status: Int,
        val mobile: String?,
        val description: String,
        val created_at: Long,
        val profile_image: String?,
        val address: String?,
        val email: String?,
        val lang: String?,
        val blocked_by_customer: Boolean,
    )

    // -----------------------

    @Keep
    @JsonClass(generateAdapter = true)
    data class PushTransactionsCommandsRequest(val operations: List<ApiTransactionCommand>)

    @Keep
    @JsonClass(generateAdapter = true)
    data class PushTransactionsCommandsResponse(val operation_responses: List<OperationResponseForTransactions>)

    @Keep
    @JsonClass(generateAdapter = true)
    data class OperationResponseForTransactions(
        val id: String, // commandId
        val status: Int,
        val error: Error?,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class Error(
        val code: Int,
        val description: String?,
    )

    enum class Status(val value: Int) {
        FAILURE(0), SUCCESS(1);
    }

    enum class ErrorCodes(val value: Int) {
        CONFLICT(409), INTERNAL(500), DEPENDENCY(600)
    }

    @Keep
    @JsonClass(generateAdapter = true)
    data class AddCustomerRequest(
        val mobile: String?,
        val description: String,
        val reactivate: Boolean,
        val profile_image: String?,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class ApiCustomer(
        val id: String,
        val status: Int,
        val user_id: String?,
        val mobile: String?,
        val description: String,
        val created_at: String,
        val txn_start_time: Long?,
        val updated_at: String?,
        val balance: Float?,
        val balance_v2: Long,
        val tx_count: Long,
        val last_activity: String?,
        val last_payment: String?,
        val account_url: String?,
        val profile_image: String?,
        val address: String?,
        val email: String?,
        val registered: Boolean,
        val txn_alert_enabled: Boolean,
        val lang: String?,
        val reminder_mode: String?,
        val due_custom_date: String?,
        val due_reminder_enabled_set: Boolean?,
        val due_credit_period_set: Boolean?,
        val is_live_sales: Boolean,
        val add_transaction_restricted: Boolean,
        val state: Int,
        val blocked_by_customer: Boolean,
        val restrict_contact_sync: Boolean,
        val last_reminder_sent: String?
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class UpdateCustomerRequest(
        val mobile: String?,
        val description: String,
        val address: String?,
        val profile_image: String?,
        val lang: String?,
        val reminder_mode: String?,
        val txn_alert_enabled: Boolean,
        val update_txn_alert_enabled: Boolean,
        val due_custom_date: Long?,
        val update_due_custom_date: Boolean,
        val delete_due_custom_date: Boolean,
        val update_add_transaction_restricted: Boolean,
        val add_transaction_restricted: Boolean,
        val state: Int,
        val update_state: Boolean,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class GetTransactionAmountHistoryRequest(
        val transaction_id: String,
    )

    @Keep
    @JsonClass(generateAdapter = true)
    class GetTransactionAmountHistoryResponse(
        @Json(name = "transaction")
        var transaction: TransactionAmountHistory,
    )

    @JsonClass(generateAdapter = true)
    data class TransactionAmountHistory(
        @Json(name = "transaction_id")
        val transactionId: String,

        @Json(name = "amount")
        var amount: String? = null,

        @Json(name = "amount_updated")
        var amountUpdated: Boolean? = null,

        @Json(name = "amount_updated_at")
        var amountUpdatedAt: String? = null,

        @Json(name = "initial")
        var initial: Initial? = null,

        @Json(name = "history")
        var history: List<History>? = null,
    )

    @JsonClass(generateAdapter = true)
    class Initial(
        @Json(name = "amount")
        var amount: String? = null,

        @Json(name = "created_at")
        var createdAt: String? = null,
    )

    @JsonClass(generateAdapter = true)
    class History {
        @Json(name = "old_amount")
        var oldAmount: String? = null

        @Json(name = "new_amount")
        var newAmount: String? = null

        @Json(name = "created_at")
        var createdAt: String? = null
    }

    @JsonClass(generateAdapter = true)
    data class SuggestedCustomerIdsForAddTransactionResponse(
        @Json(name = "accounts")
        val accountIds: List<String>,
    )
}
