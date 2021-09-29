package `in`.okcredit.merchant.customer_ui.data.server.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SubscriptionDetailResponse(
    @SerializedName("subscription")
    val subscription: Subscription
)
