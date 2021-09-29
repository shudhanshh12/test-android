package tech.okcredit.sdk.server

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface BillApiMessages {

    @Keep
    @JsonClass(generateAdapter = true)
    data class FileData(
        val bill_file: BillFile
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class ListBillsRequest(
        val start_time_ms: Long,
        val exclude_deleted: Boolean,
        val order_by: Int
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class Error(
        val code: Int,
        val description: String?
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class ListBillsResponse(
        val type: Int,
        val list_data: ListData?,
        val file_data: FileData?
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class ListData(
        @Json(name = "bills")
        val billsList: List<ServerBill>
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class BillFile(
        val id: String,
        val merchant_id: String,
        val status: Int?,
        val encryption_key: String,
        val file: String,
        val req: ListBillsRequest

    )

    // this same class is used for all CRUD operation so major entites like bill can be null also
    @Keep
    @JsonClass(generateAdapter = true)
    data class BillOperation(
        val id: String,
        val type: Int,
        val path: String?,
        @field:Json(name = "bill")
        val serverBill: ServerBill? = null,
        @field:Json(name = "bill_doc")
        val serverBillDoc: ServerBillDoc? = null,
        val timestamp: Long,
        val mask: List<String>? = null,
        val bill_id: String,
        val bill_doc_id: String? = null

    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class OperationResponse(
        val id: String, // commandId
        val status: Int,
        val error: Error?
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class BillSyncResponse(val operation_responses: List<OperationResponse>)

    @Keep
    @JsonClass(generateAdapter = true)
    data class ServerBillDoc(
        val id: String,
        val url: String,
        val created_at_ms: String,
        val updated_at_ms: String? = null,
        val deleted_at_ms: String? = null
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class BillSyncRequest(val operations: List<BillOperation>)

    @Keep
    @JsonClass(generateAdapter = true)
    data class GetBillFileResponse(
        val bill_file: BillFile
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class GetBillFileRequest(
        val id: String
    )

    @Keep
    @JsonClass(generateAdapter = true)
    data class ServerBill(
        val id: String,
        val transaction_id: String? = null,
        val account_id: String? = null,
        val created_by_me: Boolean = false,
        val note: String? = null,
        val amount: String? = null,
        val bill_date_ms: String? = null,
        @field:Json(name = "docs")
        val serverBillDocList: List<ServerBillDoc>? = null,
        val created_at_ms: String,
        val updated_at_ms: String? = null,
        val deleted_at_ms: String? = null,
        val deleted: Boolean = false,
        // 1 credit 2 payment 0 non txn
        val transaction_type: Int? = null,
        val type: Int? = null
    )
}
