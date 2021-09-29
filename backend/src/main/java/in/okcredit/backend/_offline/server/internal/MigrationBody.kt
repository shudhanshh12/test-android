package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName

class MigrationBody(
    @SerializedName("merchant_id") private val merchantId: String,
    @SerializedName(
        "account_id"
    ) private val accountId: String,
    @SerializedName("dest") private val destination: Int
)
