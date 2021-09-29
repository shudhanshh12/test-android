package `in`.okcredit.merchant.core.model

import `in`.okcredit.merchant.core.common.Timestamp
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class TransactionImage(
    val id: String,
    val url: String,
    val transactionId: String,
    val createdAt: Timestamp
)
