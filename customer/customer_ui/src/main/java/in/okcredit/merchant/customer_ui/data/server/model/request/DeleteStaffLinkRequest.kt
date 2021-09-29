package `in`.okcredit.merchant.customer_ui.data.server.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DeleteStaffLinkRequest(
    @SerializedName("link_id")
    val linkId: String
)
