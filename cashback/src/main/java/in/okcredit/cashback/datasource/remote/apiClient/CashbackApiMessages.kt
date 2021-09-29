package `in`.okcredit.cashback.datasource.remote.apiClient

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class CashbackMessageDetailsDto(
    @Json(name = "is_first_transaction")
    val isFirstTransaction: Boolean,

    @Json(name = "cashback_amount")
    val cashbackAmount: Int,

    @Json(name = "minimum_amount")
    val minimumPaymentAmount: Int,
)
