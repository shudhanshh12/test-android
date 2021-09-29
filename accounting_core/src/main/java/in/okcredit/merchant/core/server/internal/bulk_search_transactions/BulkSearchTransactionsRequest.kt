package `in`.okcredit.merchant.core.server.internal.bulk_search_transactions

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class BulkSearchTransactionsRequest(
    @Json(name = "action_id")
    val actionId: String,
    @Json(name = "transactions")
    val transactionIds: List<String>
)
