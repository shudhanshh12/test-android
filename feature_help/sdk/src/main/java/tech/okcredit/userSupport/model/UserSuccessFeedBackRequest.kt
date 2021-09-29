package tech.okcredit.userSupport.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UserSuccessFeedBackRequest(
    @SerializedName("merchant_id")
    val merchantId: String,
    val message: String,
    val feedback_type: String
)
