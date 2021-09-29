package `in`.okcredit.merchant.customer_ui.data.server.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SubscriptionListResponse(
    @SerializedName("subscriptions")
    val subscriptions: List<Subscription>
)
