package `in`.okcredit.merchant.customer_ui.data.server.model.request

import com.google.gson.annotations.SerializedName

data class AddSubscriptionRequest(
    @SerializedName("account_id")
    val account_id: String,
    @SerializedName("frequency")
    val frequency: Int,
    @SerializedName("start_date")
    val startDate: Long?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("week")
    val week: List<Int>?,
    @SerializedName("amount")
    val amount: Long,
)
