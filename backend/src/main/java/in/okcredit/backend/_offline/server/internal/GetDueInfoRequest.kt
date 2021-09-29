package `in`.okcredit.backend._offline.server.internal

import com.google.gson.annotations.SerializedName

class GetDueInfoRequest(
    @SerializedName("seller_id") var merchantId: String,
    @SerializedName(
        "account_id"
    ) var customerId: String?
)
