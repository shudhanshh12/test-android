package `in`.okcredit.merchant.core.server.internal.quick_add_transaction

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class QuickAddTransactionRequest(
    @Json(name = "merchant_id")
    val merchantId: String,
    val customer: QuickAddCustomerRequestModel,
    val transaction: QuickAddTransactionModel
)
