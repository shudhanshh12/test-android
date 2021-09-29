package `in`.okcredit.merchant.customer_ui.data.server.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EditStaffLinkRequest(
    @SerializedName("account_ids")
    val accountIds: List<String>,
    @SerializedName("action")
    val action: String, // "add/delete"
    @SerializedName("link_id")
    val linkId: String
)

enum class EditAction(val value: String) {
    ADD("add"),
    DELETE("delete")
}
