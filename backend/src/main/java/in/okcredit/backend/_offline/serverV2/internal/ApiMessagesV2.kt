package `in`.okcredit.backend._offline.serverV2.internal

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

interface ApiMessagesV2 {

    companion object {
        const val TYPE_LIST = 0
        const val TYPE_FILE = 1

        const val ROLE_BUYER = 2 // Customers

        const val FILE_DOWNLOAD_IN_PROGRESS = 0
        const val FILE_DOWNLOAD_COMPLETED = 1
    }

    @Keep
    data class Transaction(
        val id: String,
        val account_id: String, // (eg: customer_id, supplier_id)
        val type: Int, // (UNKNOWN = 0, CREDIT = 1, PAYMENT = 2)
        val amount: Long, // amount of this transaction (absolute value, always positive)
        val creator_role: Int, // denotes role of a merchant in an account (UNKNOWN = 0, SELLER = 1, BUYER = 2)
        val create_time: Long?,
        val deleted: Boolean, // true if this transaction is deleted (deleted transactions have no effect on account summary)
        val deleter_role: Int?,
        val delete_time: Long?,
        val note: String?,
        val receipt_url: String,
        val bill_date: Long?,
        val alert_sent_by_creator: Boolean, // true if alert was already sent by the creator before this transaction was synced
        val collection_id: String,
        val update_time: Long,
        val images: List<TransactionImageV2>,
        val transaction_state: Int,
        val tx_category: Int,
    )

    @Keep
    data class TransactionImageV2(
        @SerializedName("id") var id: String?,
        @SerializedName("request_id") var request_id: String,
        @SerializedName("transaction_id") val transaction_id: String?,
        @SerializedName("url") var url: String,
        @SerializedName("create_time") var create_time: Long
    )

    @Keep
    data class GetTransactionsRequest(
        val txn_req: TransactionsRequest
    )

    @Keep
    data class TransactionsRequest(
        val type: Int, // type of transactions (MERCHANT = 0, ACCOUNT = 1)
        val account_id: String? = null, // filter to get specific account transactions.
        val role: Int? = null, // denotes role of a merchant in an account (UNKNOWN = 0, SELLER = 1, BUYER = 2)
        val start_time: Long? = null,
        val end_time: Long? = null,
        val exclude_deleted: Long? = null,
        val order_by: Int? = 1 // denotes ordering of transactions (UNKNOWN = 0, UPDATE_TIME = 1, BILL_DATE = 2)
    )

    @Keep
    data class GetTransactionsResponse(
        val type: Int, // type of transactions (LIST = 0, FILE = 1)
        val list_data: ListData?, // transactions info if type is list.
        val file_data: FileData? // denotes role of a merchant in an account (UNKNOWN = 0, SELLER = 1, BUYER = 2)
    )

    @Keep
    data class ListData(
        val transactions: List<Transaction>
    )

    @Keep
    data class FileData(
        val txn_file: TransactionFile
    )

    @Keep
    data class TransactionFile(
        val id: String,
        val status: Int?, // denotes role of a merchant in an account (FILE_DOWNLOAD_IN_PROGRESS = 0, FILE_DOWNLOAD_COMPLETED = 1)
        val encryption_key: String?,
        val file: String?
    )

    @Keep
    data class GetTransactionFileRequest(
        val id: String
    )

    @Keep
    data class GetTransactionFileResponse(
        val txn_file: TransactionFile
    )

    @Keep
    data class PatchTransactionRequest(val note: String)

    @Keep
    data class PatchAmountTransactionRequest(val amount: Long)
}
