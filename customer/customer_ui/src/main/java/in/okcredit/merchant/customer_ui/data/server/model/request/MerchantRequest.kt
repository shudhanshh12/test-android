package `in`.okcredit.merchant.customer_ui.data.server.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MerchantRequest(
    @SerializedName("merchant_id")
    val businessId: String,
    @SerializedName("account_id")
    val account_id: String?
)
