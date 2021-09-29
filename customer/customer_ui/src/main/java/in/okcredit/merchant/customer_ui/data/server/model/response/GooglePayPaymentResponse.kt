package `in`.okcredit.merchant.customer_ui.data.server.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GooglePayPaymentResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: String?,
)
