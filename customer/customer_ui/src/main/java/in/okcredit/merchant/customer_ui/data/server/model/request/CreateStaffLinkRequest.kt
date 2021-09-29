package `in`.okcredit.merchant.customer_ui.data.server.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreateStaffLinkRequest(
    @SerializedName("account_ids")
    val accounts: List<String>
)
