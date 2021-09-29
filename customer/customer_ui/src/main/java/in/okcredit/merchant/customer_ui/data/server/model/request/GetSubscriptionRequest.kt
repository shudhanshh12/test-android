package `in`.okcredit.merchant.customer_ui.data.server.model.request

import com.google.gson.annotations.SerializedName

data class GetSubscriptionRequest(
    @SerializedName("subscription_id")
    val subscriptionId: String
)
