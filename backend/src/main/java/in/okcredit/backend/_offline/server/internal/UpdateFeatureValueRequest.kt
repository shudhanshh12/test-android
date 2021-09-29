package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName

class UpdateFeatureValueRequest(
    @SerializedName("account_id") private val accountId: String,
    @SerializedName(
        "action"
    ) private val action: Int
)
