package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName

class AddDiscountRequest(
    @SerializedName("request_id") private val requestId: String,
    @SerializedName(
        "account_id"
    ) private val accountId: String,
    @SerializedName("amount") private val amount: Long,
    @SerializedName(
        "note"
    ) private val note: String?
)
